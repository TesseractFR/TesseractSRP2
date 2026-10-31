package onl.tesseract.srp.contract.domain.port.userside;

import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.model.Quality;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.contract.ContractType;
import onl.tesseract.srp.contract.domain.model.contract.DeliveryRefusal;
import onl.tesseract.srp.contract.domain.model.exception.PaymentFailedException;
import onl.tesseract.srp.contract.domain.port.serverside.ContractMessagingRepository;
import onl.tesseract.srp.contract.domain.port.serverside.ContractRepository;
import onl.tesseract.srp.contract.domain.port.serverside.PlayerInfoRepository;
import onl.tesseract.srp.contract.domain.port.serverside.PlayerInventoryRepository;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static onl.tesseract.srp.contract.domain.model.contract.DeliveryRefusal.*;

public class ContractService {

    private final ContractRepository contracts;
    private final PlayerInventoryRepository inventory;
    private final ContractGenerator generator;
    private final PlayerInfoRepository players;
    private final ContractMessagingRepository messaging;

    public ContractService(ContractRepository contracts, PlayerInventoryRepository inventory, ContractGenerator generator, PlayerInfoRepository players, ContractMessagingRepository messaging) {
        this.contracts = contracts;
        this.inventory = inventory;
        this.generator = generator;
        this.players = players;
        this.messaging = messaging;
    }

    public void buyScroll(PlayerID player) {
        int ticketsBefore = getTickets(player);
        if (ticketsBefore < 1) {
            messaging.sendPurchaseNoTicket(player);
            return;
        }
        if (!inventory.canReceiveScroll(player)) {
            messaging.sendPurchaseInventoryFull(player);
            return;
        }
        Optional<Contract> draft = generator.generate(player);
        if (draft.isEmpty()) {
            messaging.sendPurchaseNoContractAvailable(player);
            return;
        }
        players.saveTickets(player, ticketsBefore - 1);
        Contract saved = saveOrRefund(player, ticketsBefore, draft.get());
        if (!inventory.giveScroll(player, saved)) {
            contracts.delete(saved.id());
            players.saveTickets(player, ticketsBefore);
            messaging.sendPurchaseInventoryFull(player);
            return;
        }
        messaging.sendPurchaseSucceeded(player, saved, ticketsBefore - 1);
    }

    public void deliver(PlayerID player, long contractId, String npcJob) {
        Optional<Contract> found = prepareDelivery(player, contractId, npcJob);
        if (found.isEmpty()) {
            return;
        }
        Contract contract = found.get();
        if (!inventory.hasScroll(player, contractId)) {
            refuse(player, npcJob, contractId, INVALID_SCROLL);
            return;
        }
        int have = inventory.countAtLeast(player, contract.material(), contract.minimalQuality());
        if (have < contract.quantity()) {
            boolean lowerQualityExists = inventory.countAtLeast(player, contract.material(), Quality.POOR) > have;
            messaging.sendDeliveryRefused(player, npcJob, lowerQualityExists ? NOT_ENOUGH_QUALITY : MISSING_ITEMS, contract.quantity() - have);
            return;
        }
        withdrawAndPay(player, npcJob, contract);
    }

    public int moneyBonus(PlayerID deliverer, Contract contract) {
        if (contract.type() != ContractType.HARVEST) {
            return 0;
        }
        double rate = Math.max(0, players.getMoneyBonus(deliverer, contract.job(), contract.material()));
        return (int) Math.round(contract.reward() * rate);
    }

    public void noScrollInHand(PlayerID player, String npcJob) {
        messaging.sendNoScrollInHand(player, npcJob);
    }

    public void useScroll(PlayerID player, long contractId) {
        boolean valid = contracts.findById(contractId).map(c -> !c.isExpired(Instant.now())).orElse(false);
        if (!valid && inventory.removeScroll(player, contractId)) {
            messaging.sendScrollDestroyed(player);
        }
    }

    public int getDailyQuota(PlayerID player) {
        return players.getDailyTickets(player);
    }

    public int getTickets(PlayerID player) {
        return players.findTickets(player).orElseGet(() -> players.getDailyTickets(player));
    }

    public void showTickets(PlayerID player) {
        messaging.sendTicketsShown(player, getTickets(player), getDailyQuota(player));
    }

    public void giveTicketsAsStaff(PlayerID staff, PlayerID target, int amount) {
        if (amount <= 0) {
            messaging.sendStaffInvalidAmount(staff);
            return;
        }
        players.saveTickets(target, getTickets(target) + amount);
        messaging.sendStaffTicketsGiven(staff, target, amount);
    }

    public void showTicketsAsStaff(PlayerID staff, PlayerID target) {
        messaging.sendStaffTicketsShown(staff, target, getTickets(target), getDailyQuota(target));
    }

    public void renewDaily(PlayerID player) {
        int quota = players.getDailyTickets(player);
        if (getTickets(player) != quota) {
            players.saveTickets(player, quota);
        }
    }

    public int purgeExpired() {
        return contracts.deleteExpired(Instant.now());
    }

    public Optional<Contract> prepareDelivery(PlayerID player, long contractId, String npcJob) {
        Optional<Contract> found = contracts.findById(contractId);
        if (found.isEmpty()) {
            refuse(player, npcJob, contractId, INVALID_SCROLL);
            return Optional.empty();
        } else if (!found.get().isForJob(npcJob)) {
            refuse(player, npcJob, contractId, WRONG_CLIENT);
            return Optional.empty();
        } else if (found.get().isExpired(Instant.now())) {
            refuse(player, npcJob, contractId, EXPIRED);
            return Optional.empty();
        }
        return found;
    }

    private void refuse(PlayerID player, String npcJob, long contractId, DeliveryRefusal why) {
        if (why == EXPIRED) {
            contracts.delete(contractId);
        }
        messaging.sendDeliveryRefused(player, npcJob, why, 0);
    }

    private void withdrawAndPay(PlayerID player, String npcJob, Contract contract) {
        if (!inventory.removeScroll(player, contract.id())) {
            messaging.sendDeliveryRefused(player, npcJob, INVALID_SCROLL, 0);
            return;
        }
        Map<Quality, Integer> removed = inventory.removeAtLeast(player, contract.material(), contract.minimalQuality(), contract.quantity());
        if (removed.isEmpty()) {
            inventory.giveScroll(player, contract);
            messaging.sendDeliveryRefused(player, npcJob, MISSING_ITEMS, contract.quantity());
            return;
        }
        int payout = 0;
        boolean paid = false;
        DeliveryRefusal refusal = INVALID_SCROLL;
        try {
            payout = contract.reward() + moneyBonus(player, contract);
            paid = contracts.settle(contract.id(), player, payout);
        } catch (PaymentFailedException e) {
            refusal = PAYMENT_FAILED;
        } finally {
            if (!paid) {
                inventory.giveBack(player, contract.material(), removed);
                inventory.giveScroll(player, contract);
            }
        }
        if (paid) {
            messaging.sendDeliverySucceeded(player, npcJob, payout);
        } else {
            messaging.sendDeliveryRefused(player, npcJob, refusal, 0);
        }
    }

    private Contract saveOrRefund(PlayerID player, int ticketsBefore, Contract draft) {
        try {
            return contracts.save(draft);
        } catch (RuntimeException e) {
            players.saveTickets(player, ticketsBefore);
            throw e;
        }
    }
}