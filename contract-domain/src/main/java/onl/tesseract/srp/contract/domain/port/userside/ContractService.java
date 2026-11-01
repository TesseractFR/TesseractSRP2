package onl.tesseract.srp.contract.domain.port.userside;

import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.model.Quality;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.contract.ContractType;
import onl.tesseract.srp.contract.domain.model.contract.DeliveryRefusal;
import onl.tesseract.srp.contract.domain.model.exception.PaymentFailedException;
import onl.tesseract.srp.contract.domain.port.serverside.ContractMessagingRepository;
import onl.tesseract.srp.contract.domain.port.serverside.ContractRepository;
import onl.tesseract.srp.contract.domain.port.serverside.ContractScrollRepository;
import onl.tesseract.srp.contract.domain.port.serverside.PlayerInfoRepository;
import onl.tesseract.srp.contract.domain.port.serverside.PlayerMaterialRepository;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static onl.tesseract.srp.contract.domain.model.contract.DeliveryRefusal.*;

public class ContractService {

    private final ContractRepository contractRepository;
    private final PlayerMaterialRepository playerMaterialRepository;
    private final ContractScrollRepository contractScrollRepository;
    private final ContractGenerator contractGenerator;
    private final PlayerInfoRepository playerInfoRepository;
    private final ContractMessagingRepository contractMessagingRepository;

    public ContractService(ContractRepository contractRepository, PlayerMaterialRepository playerMaterialRepository,
                           ContractScrollRepository contractScrollRepository, ContractGenerator contractGenerator,
                           PlayerInfoRepository playerInfoRepository,
                           ContractMessagingRepository contractMessagingRepository) {
        this.contractRepository = contractRepository;
        this.playerMaterialRepository = playerMaterialRepository;
        this.contractScrollRepository = contractScrollRepository;
        this.contractGenerator = contractGenerator;
        this.playerInfoRepository = playerInfoRepository;
        this.contractMessagingRepository = contractMessagingRepository;
    }

    public void buyScroll(PlayerID player) {
        if (!hasTicket(player)) {
            contractMessagingRepository.sendPurchaseNoTicket(player);
            return;
        }
        if (!contractScrollRepository.canReceive(player)) {
            contractMessagingRepository.sendPurchaseInventoryFull(player);
            return;
        }
        Optional<Contract> draftContract = contractGenerator.generate(player);
        if (draftContract.isEmpty()) {
            contractMessagingRepository.sendPurchaseNoContractAvailable(player);
            return;
        }

        withdrawTicket(player);
        Contract savedContract = saveContractOrRefundTicket(player, draftContract.get());

        if (!giveScroll(player, savedContract)) {
            cancelPurchase(player, savedContract);
            contractMessagingRepository.sendPurchaseInventoryFull(player);
            return;
        }
        contractMessagingRepository.sendPurchaseSucceeded(player, savedContract, getTickets(player));
    }

    public Optional<Contract> checkDeliverable(PlayerID player, long contractId, String npcJob) {
        Optional<Contract> foundContract = contractRepository.findById(contractId);
        if (foundContract.isEmpty()) {
            refuse(player, npcJob, contractId, INVALID_SCROLL);
            return Optional.empty();
        }
        Contract contract = foundContract.get();
        if (!contract.isForJob(npcJob)) {
            refuse(player, npcJob, contractId, WRONG_CLIENT);
            return Optional.empty();
        }
        if (contract.isExpired(Instant.now())) {
            refuse(player, npcJob, contractId, EXPIRED);
            return Optional.empty();
        }
        return foundContract;
    }

    public void deliver(PlayerID player, long contractId, String npcJob) {
        Optional<Contract> foundContract = checkDeliverable(player, contractId, npcJob);
        if (foundContract.isEmpty()) {
            return;
        }
        Contract contract = foundContract.get();

        if (!contractScrollRepository.has(player, contractId)) {
            refuse(player, npcJob, contractId, INVALID_SCROLL);
            return;
        }
        int itemsOwned = countOwnedItems(player, contract);
        if (itemsOwned < contract.quantity()) {
            refuseMissingItems(player, npcJob, contract, itemsOwned);
            return;
        }
        withdrawAndPay(player, npcJob, contract);
    }

    private boolean giveScroll(PlayerID player, Contract contract) {
        return contractScrollRepository.give(player, contract, moneyBonus(player, contract) > 0);
    }

    public int countOwnedItems(PlayerID player, Contract contract) {
        return playerMaterialRepository.countAtLeast(player, contract.material(), contract.minimalQuality());
    }

    public int moneyBonus(PlayerID deliverer, Contract contract) {
        if (contract.type() != ContractType.HARVEST) {
            return 0;
        }
        double bonusRate = Math.max(0, playerInfoRepository.getMoneyBonus(deliverer, contract.job(), contract.material()));
        return (int) Math.round(contract.reward() * bonusRate);
    }

    public void noScrollInHand(PlayerID player, String npcJob) {
        contractMessagingRepository.sendNoScrollInHand(player, npcJob);
    }

    public void useScroll(PlayerID player, long contractId) {
        boolean contractStillValid = contractRepository.findById(contractId)
                .map(contract -> !contract.isExpired(Instant.now()))
                .orElse(false);
        if (!contractStillValid && contractScrollRepository.remove(player, contractId)) {
            contractMessagingRepository.sendScrollDestroyed(player);
        }
    }

    public int purgeExpired() {
        return contractRepository.deleteExpired(Instant.now());
    }

    public void reportExpiredScrolls(PlayerID player, int count) {
        purgeExpired();
        contractMessagingRepository.sendScrollsExpired(player, count);
    }

    public int getDailyQuota(PlayerID player) {
        return playerInfoRepository.getDailyQuota(player);
    }

    public int getTickets(PlayerID player) {
        return playerInfoRepository.findTickets(player).orElseGet(() -> getDailyQuota(player));
    }

    public void showTickets(PlayerID player) {
        contractMessagingRepository.sendTicketsShown(player, getTickets(player), getDailyQuota(player));
    }

    public void giveTicketsAsStaff(PlayerID staff, PlayerID target, int amount) {
        if (amount <= 0) {
            contractMessagingRepository.sendStaffInvalidAmount(staff);
            return;
        }
        addTickets(target, amount);
        contractMessagingRepository.sendStaffTicketsGiven(staff, target, amount);
    }

    public void showTicketsAsStaff(PlayerID staff, PlayerID target) {
        contractMessagingRepository.sendStaffTicketsShown(staff, target, getTickets(target), getDailyQuota(target));
    }

    public void renewDaily(PlayerID player) {
        int quota = getDailyQuota(player);
        if (getTickets(player) != quota) {
            playerInfoRepository.saveTickets(player, quota);
        }
    }

    private boolean hasTicket(PlayerID player) {
        return getTickets(player) >= 1;
    }

    private void addTickets(PlayerID player, int amount) {
        playerInfoRepository.saveTickets(player, getTickets(player) + amount);
    }

    private void withdrawTicket(PlayerID player) {
        playerInfoRepository.saveTickets(player, getTickets(player) - 1);
    }

    private Contract saveContractOrRefundTicket(PlayerID player, Contract draftContract) {
        try {
            return contractRepository.save(draftContract);
        } catch (RuntimeException e) {
            addTickets(player, 1);
            throw e;
        }
    }

    private void cancelPurchase(PlayerID player, Contract savedContract) {
        contractRepository.delete(savedContract.id());
        addTickets(player, 1);
    }

    private void refuse(PlayerID player, String npcJob, long contractId, DeliveryRefusal refusalReason) {
        if (refusalReason == EXPIRED) {
            contractRepository.delete(contractId);
        }
        contractMessagingRepository.sendDeliveryRefused(player, npcJob, refusalReason, 0);
    }

    private void refuseMissingItems(PlayerID player, String npcJob, Contract contract, int itemsOwned) {
        int itemsOwnedAnyQuality = playerMaterialRepository.countAtLeast(player, contract.material(), Quality.POOR);
        DeliveryRefusal refusalReason = itemsOwnedAnyQuality > itemsOwned ? NOT_ENOUGH_QUALITY : MISSING_ITEMS;
        contractMessagingRepository.sendDeliveryRefused(player, npcJob, refusalReason, contract.quantity() - itemsOwned);
    }

    private void withdrawAndPay(PlayerID player, String npcJob, Contract contract) {
        int payout = contract.reward() + moneyBonus(player, contract);

        Map<Quality, Integer> removedItems = takeScrollAndItemsOrRefuse(player, npcJob, contract);
        if (removedItems.isEmpty()) {
            return;
        }
        if (payOrRefuse(player, npcJob, contract, payout, removedItems)) {
            contractMessagingRepository.sendDeliverySucceeded(player, npcJob, payout);
        }
    }

    private Map<Quality, Integer> takeScrollAndItemsOrRefuse(PlayerID player, String npcJob, Contract contract) {
        if (!contractScrollRepository.remove(player, contract.id())) {
            contractMessagingRepository.sendDeliveryRefused(player, npcJob, INVALID_SCROLL, 0);
            return Map.of();
        }
        Map<Quality, Integer> removedItems;
        try {
            removedItems = playerMaterialRepository.removeAtLeast(player, contract.material(),
                    contract.minimalQuality(), contract.quantity());
        } catch (RuntimeException e) {
            giveScroll(player, contract);
            throw e;
        }
        if (removedItems.isEmpty()) {
            giveScroll(player, contract);
            contractMessagingRepository.sendDeliveryRefused(player, npcJob, MISSING_ITEMS, contract.quantity());
        }
        return removedItems;
    }

    private boolean payOrRefuse(PlayerID player, String npcJob, Contract contract, int payout,
                                Map<Quality, Integer> removedItems) {
        boolean isPaid;
        try {
            isPaid = contractRepository.settle(contract.id(), player, payout);
        } catch (PaymentFailedException e) {
            giveBackToPlayer(player, contract, removedItems);
            contractMessagingRepository.sendDeliveryRefused(player, npcJob, PAYMENT_FAILED, 0);
            return false;
        } catch (RuntimeException e) {
            giveBackToPlayer(player, contract, removedItems);
            throw e;
        }
        if (!isPaid) {
            giveBackToPlayer(player, contract, removedItems);
            contractMessagingRepository.sendDeliveryRefused(player, npcJob, INVALID_SCROLL, 0);
        }
        return isPaid;
    }

    private void giveBackToPlayer(PlayerID player, Contract contract, Map<Quality, Integer> removedItems) {
        playerMaterialRepository.giveBack(player, contract.material(), removedItems);
        giveScroll(player, contract);
    }
}