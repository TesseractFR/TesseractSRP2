package onl.tesseract.srp.contract.domain.port.userside;

import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.contract.ContractClient;
import onl.tesseract.srp.contract.domain.model.contract.ContractId;
import onl.tesseract.srp.contract.domain.model.exception.ContractExpiredException;
import onl.tesseract.srp.contract.domain.model.exception.ContractNotFoundException;
import onl.tesseract.srp.contract.domain.model.exception.ContractScrollNotOwnedException;
import onl.tesseract.srp.contract.domain.model.exception.MissingContractItemsException;
import onl.tesseract.srp.contract.domain.model.exception.PaymentFailedException;
import onl.tesseract.srp.contract.domain.model.exception.WrongContractClientException;
import onl.tesseract.srp.contract.domain.port.serverside.ContractRepository;
import onl.tesseract.srp.contract.domain.port.serverside.ContractScrollPort;
import onl.tesseract.srp.contract.domain.port.serverside.JobMoneyBonusPort;
import onl.tesseract.srp.contract.domain.port.serverside.PaymentPort;
import onl.tesseract.srp.contract.domain.port.serverside.PlayerMaterialPort;

import java.time.Clock;
import java.util.Optional;

/** Consulte les contrats et traite leur livraison à un PNJ. */
public class ContractDeliveryService {

    private final ContractRepository contractRepository;
    private final ContractScrollPort contractScrollPort;
    private final PlayerMaterialPort playerMaterialPort;
    private final JobMoneyBonusPort jobMoneyBonusPort;
    private final PaymentPort paymentPort;
    private final Clock clock;

    public ContractDeliveryService(ContractRepository contractRepository, ContractScrollPort contractScrollPort,
                                   PlayerMaterialPort playerMaterialPort, JobMoneyBonusPort jobMoneyBonusPort,
                                   PaymentPort paymentPort, Clock clock) {
        this.contractRepository = contractRepository;
        this.contractScrollPort = contractScrollPort;
        this.playerMaterialPort = playerMaterialPort;
        this.jobMoneyBonusPort = jobMoneyBonusPort;
        this.paymentPort = paymentPort;
        this.clock = clock;
    }

    /**
     * Récupère le contrat à présenter au PNJ, avant l'ouverture du menu de livraison.
     *
     * @return le contrat, destiné au PNJ
     * @throws ContractNotFoundException si le contrat n'existe plus
     * @throws WrongContractClientException si le PNJ n'est pas le destinataire du contrat
     */
    public Contract getDeliverableContract(ContractId contractId, ContractClient npcClient) {
        Contract contract = getContract(contractId);
        if (!contract.isFor(npcClient)) {
            throw new WrongContractClientException(contractId, contract.client(), npcClient);
        }
        return contract;
    }

    /** @return nombre de matériaux du contrat possédés par le joueur à la qualité minimale requise */
    public int countOwnedItems(PlayerId player, Contract contract) {
        return playerMaterialPort.countAtLeast(player, contract.material(), contract.minimalQuality());
    }

    /** @return {@code true} si le joueur possède assez de matériaux pour livrer le contrat */
    public boolean hasEnoughItems(PlayerId player, Contract contract) {
        return countOwnedItems(player, contract) >= contract.quantity();
    }

    /**
     * Calcule le bonus de métier que toucherait le livreur.
     *
     * @return le bonus, ou {@link Optional#empty()} si le contrat n'en donne pas ou s'il est nul
     */
    public Optional<MoneyValue> findMoneyBonus(PlayerId deliverer, Contract contract) {
        return contract.computeMoneyBonus(
                jobMoneyBonusPort.getMoneyBonusRate(deliverer, contract.client(), contract.material()));
    }

    /**
     * Livre le contrat au PNJ : retire le parchemin et les matériaux, supprime le contrat, puis
     * verse la récompense et le bonus de métier du livreur.
     *
     * @return somme versée au livreur
     * @throws ContractNotFoundException si le contrat n'existe plus
     * @throws ContractExpiredException si le contrat a expiré (il est alors supprimé)
     * @throws WrongContractClientException si le PNJ n'est pas le destinataire du contrat
     * @throws ContractScrollNotOwnedException si le livreur ne possède pas le parchemin
     * @throws MissingContractItemsException si le livreur n'a pas assez de matériaux
     * @throws PaymentFailedException si le versement échoue, après le retrait des objets
     */
    public MoneyValue deliver(PlayerId deliverer, ContractId contractId, ContractClient npcClient) {
        Contract contract = getContract(contractId);
        if (contract.isExpiredAt(clock.instant())) {
            contractRepository.delete(contractId);
            throw new ContractExpiredException(contractId);
        }
        if (!contract.isFor(npcClient)) {
            throw new WrongContractClientException(contractId, contract.client(), npcClient);
        }
        if (!contractScrollPort.hasScroll(deliverer, contractId)) {
            throw new ContractScrollNotOwnedException(deliverer, contractId);
        }
        int ownedQuantity = countOwnedItems(deliverer, contract);
        if (ownedQuantity < contract.quantity()) {
            throw new MissingContractItemsException(deliverer, contractId, contract.quantity() - ownedQuantity);
        }

        if (!contractScrollPort.removeScroll(deliverer, contractId)) {
            throw new ContractScrollNotOwnedException(deliverer, contractId);
        }
        playerMaterialPort.removeAtLeast(deliverer, contract.material(), contract.minimalQuality(), contract.quantity());
        contractRepository.delete(contractId);

        // Le bonus est celui du livreur, pas de l'acheteur : il est calculé au moment de la livraison.
        MoneyValue payment = findMoneyBonus(deliverer, contract)
                .map(bonus -> new MoneyValue(contract.reward().value().add(bonus.value())))
                .orElse(contract.reward());
        paymentPort.pay(deliverer, payment);
        return payment;
    }

    private Contract getContract(ContractId contractId) {
        return contractRepository.findById(contractId)
                .orElseThrow(() -> new ContractNotFoundException(contractId));
    }
}
