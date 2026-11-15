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

import java.util.Optional;

/** Cas d'usage de consultation et de livraison d'un contrat à un PNJ. */
public interface ContractDeliveryUseCase {
    /** @return le contrat, ou {@link Optional#empty()} s'il n'existe pas ou a été supprimé */
    Optional<Contract> findContract(ContractId contractId);

    /** @return nombre de matériaux du contrat possédés par le joueur à la qualité minimale requise */
    int countOwnedItems(PlayerId player, Contract contract);

    /** @return {@code true} si le joueur possède assez de matériaux pour livrer le contrat */
    boolean hasEnoughItems(PlayerId player, Contract contract);

    /**
     * Calcule le bonus de métier que toucherait le livreur. Seuls les contrats de récolte en donnent.
     *
     * @return le bonus, ou {@link Optional#empty()} si le contrat n'en donne pas ou s'il est nul
     */
    Optional<MoneyValue> findMoneyBonus(PlayerId deliverer, Contract contract);

    /**
     * Livre le contrat au PNJ : retire le parchemin et les matériaux, supprime le contrat, puis
     * verse la récompense et le bonus de métier.
     *
     * @return somme versée au livreur
     * @throws ContractNotFoundException si le contrat n'existe plus
     * @throws ContractExpiredException si le contrat a expiré (il est alors supprimé)
     * @throws WrongContractClientException si le PNJ n'est pas le destinataire du contrat
     * @throws ContractScrollNotOwnedException si le livreur ne possède pas le parchemin
     * @throws MissingContractItemsException si le livreur n'a pas assez de matériaux
     * @throws PaymentFailedException si le versement échoue, après le retrait des objets
     */
    MoneyValue deliver(PlayerId deliverer, ContractId contractId, ContractClient npcClient);
}
