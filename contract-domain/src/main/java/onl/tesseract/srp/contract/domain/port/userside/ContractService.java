package onl.tesseract.srp.contract.domain.port.userside;

import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.contract.ContractClient;
import onl.tesseract.srp.contract.domain.model.contract.ContractDraft;
import onl.tesseract.srp.contract.domain.model.contract.ContractId;
import onl.tesseract.srp.contract.domain.model.contract.ContractType;
import onl.tesseract.srp.contract.domain.model.exception.ContractExpiredException;
import onl.tesseract.srp.contract.domain.model.exception.ContractNotFoundException;
import onl.tesseract.srp.contract.domain.model.exception.ContractScrollNotOwnedException;
import onl.tesseract.srp.contract.domain.model.exception.InvalidTicketAmountException;
import onl.tesseract.srp.contract.domain.model.exception.MissingContractItemsException;
import onl.tesseract.srp.contract.domain.model.exception.NoContractAvailableException;
import onl.tesseract.srp.contract.domain.model.exception.NoContractTicketLeftException;
import onl.tesseract.srp.contract.domain.model.exception.ScrollInventoryFullException;
import onl.tesseract.srp.contract.domain.model.exception.WrongContractClientException;
import onl.tesseract.srp.contract.domain.port.serverside.ContractRepository;
import onl.tesseract.srp.contract.domain.port.serverside.ContractScrollPort;
import onl.tesseract.srp.contract.domain.port.serverside.ContractTicketRepository;
import onl.tesseract.srp.contract.domain.port.serverside.DailyTicketQuotaPort;
import onl.tesseract.srp.contract.domain.port.serverside.JobMoneyBonusPort;
import onl.tesseract.srp.contract.domain.port.serverside.PaymentPort;
import onl.tesseract.srp.contract.domain.port.serverside.PlayerMaterialPort;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.util.Optional;

/**
 * Service de l'agrégat contrat. Il implémente un cas d'usage par responsabilité : achat,
 * livraison, tickets et expiration.
 */
public class ContractService implements ContractPurchaseUseCase, ContractDeliveryUseCase, ContractTicketUseCase,
        ContractExpirationUseCase {

    private final ContractRepository contractRepository;
    private final ContractGenerator contractGenerator;
    private final ContractScrollPort contractScrollPort;
    private final PlayerMaterialPort playerMaterialPort;
    private final ContractTicketRepository contractTicketRepository;
    private final DailyTicketQuotaPort dailyTicketQuotaPort;
    private final JobMoneyBonusPort jobMoneyBonusPort;
    private final PaymentPort paymentPort;
    private final Clock clock;

    public ContractService(ContractRepository contractRepository, ContractGenerator contractGenerator,
                           ContractScrollPort contractScrollPort, PlayerMaterialPort playerMaterialPort,
                           ContractTicketRepository contractTicketRepository, DailyTicketQuotaPort dailyTicketQuotaPort,
                           JobMoneyBonusPort jobMoneyBonusPort, PaymentPort paymentPort, Clock clock) {
        this.contractRepository = contractRepository;
        this.contractGenerator = contractGenerator;
        this.contractScrollPort = contractScrollPort;
        this.playerMaterialPort = playerMaterialPort;
        this.contractTicketRepository = contractTicketRepository;
        this.dailyTicketQuotaPort = dailyTicketQuotaPort;
        this.jobMoneyBonusPort = jobMoneyBonusPort;
        this.paymentPort = paymentPort;
        this.clock = clock;
    }

    // ---- Achat

    @Override
    public Contract buyScroll(PlayerId buyer) {
        if (getTickets(buyer) < 1) {
            throw new NoContractTicketLeftException(buyer);
        }
        if (!contractScrollPort.canReceiveScroll(buyer)) {
            throw new ScrollInventoryFullException(buyer);
        }
        ContractDraft draft = contractGenerator.generate()
                .orElseThrow(() -> new NoContractAvailableException(buyer));

        withdrawTicket(buyer);
        Contract contract = createContractOrRefundTicket(buyer, draft);
        if (!contractScrollPort.giveScroll(buyer, contract, findMoneyBonus(buyer, contract).isPresent())) {
            contractRepository.delete(contract.id());
            addTickets(buyer, 1);
            throw new ScrollInventoryFullException(buyer);
        }
        return contract;
    }

    /** Le ticket est déjà retiré : il faut le rendre si l'enregistrement échoue, avant de laisser remonter l'erreur. */
    private Contract createContractOrRefundTicket(PlayerId buyer, ContractDraft draft) {
        try {
            return contractRepository.create(draft);
        } catch (RuntimeException e) {
            addTickets(buyer, 1);
            throw e;
        }
    }

    // ---- Livraison

    @Override
    public Optional<Contract> findContract(ContractId contractId) {
        return contractRepository.findById(contractId);
    }

    @Override
    public int countOwnedItems(PlayerId player, Contract contract) {
        return playerMaterialPort.countAtLeast(player, contract.material(), contract.minimalQuality());
    }

    @Override
    public boolean hasEnoughItems(PlayerId player, Contract contract) {
        return countOwnedItems(player, contract) >= contract.quantity();
    }

    @Override
    public Optional<MoneyValue> findMoneyBonus(PlayerId deliverer, Contract contract) {
        if (contract.type() != ContractType.HARVEST) {
            return Optional.empty();
        }
        double bonusRate = Math.max(0, jobMoneyBonusPort.getMoneyBonusRate(deliverer, contract.client(), contract.material()));
        BigDecimal bonus = contract.reward().value()
                .multiply(BigDecimal.valueOf(bonusRate))
                .setScale(0, RoundingMode.HALF_UP);
        return bonus.signum() > 0 ? Optional.of(new MoneyValue(bonus)) : Optional.empty();
    }

    @Override
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

    // ---- Tickets

    @Override
    public int getTickets(PlayerId player) {
        return contractTicketRepository.findTickets(player).orElseGet(() -> getDailyQuota(player));
    }

    @Override
    public int getDailyQuota(PlayerId player) {
        return dailyTicketQuotaPort.getDailyQuota(player);
    }

    @Override
    public void giveTickets(PlayerId target, int amount) {
        if (amount <= 0) {
            throw new InvalidTicketAmountException(amount);
        }
        addTickets(target, amount);
    }

    @Override
    public void renewDaily(PlayerId player) {
        int quota = getDailyQuota(player);
        if (getTickets(player) != quota) {
            contractTicketRepository.saveTickets(player, quota);
        }
    }

    private void addTickets(PlayerId player, int amount) {
        contractTicketRepository.saveTickets(player, getTickets(player) + amount);
    }

    private void withdrawTicket(PlayerId player) {
        contractTicketRepository.saveTickets(player, getTickets(player) - 1);
    }

    // ---- Expiration

    @Override
    public int purgeExpired() {
        return contractRepository.deleteExpired(clock.instant());
    }

    @Override
    public boolean isScrollValid(ContractId contractId) {
        return contractRepository.findById(contractId)
                .map(contract -> !contract.isExpiredAt(clock.instant()))
                .orElse(false);
    }
}
