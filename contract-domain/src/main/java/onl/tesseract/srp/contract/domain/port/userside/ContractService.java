package onl.tesseract.srp.contract.domain.port.userside;

import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.model.Quality;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.event.ContractDelivered;
import onl.tesseract.srp.contract.domain.model.event.ContractExpired;
import onl.tesseract.srp.contract.domain.model.event.ContractIssued;
import onl.tesseract.srp.contract.domain.model.profile.PlayerContractProfile;
import onl.tesseract.srp.contract.domain.model.result.DeliveryResult;
import onl.tesseract.srp.contract.domain.model.result.DeliveryStatus;
import onl.tesseract.srp.contract.domain.model.result.PurchaseResult;
import onl.tesseract.srp.contract.domain.model.result.PurchaseStatus;
import onl.tesseract.srp.contract.domain.model.ticket.ContractPlayerState;
import onl.tesseract.srp.contract.domain.port.serverside.*;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ContractService {

    static final String REWARD_REASON = "CONTRAT_RECOMPENSE";

    private final ContractRepository contracts;
    private final ContractPlayerStateRepository states;
    private final PlayerProfileRepository profiles;
    private final ScrollItemRepository scrolls;
    private final ItemRepository items;
    private final MoneyRepository money;
    private final ContractGenerator generator;
    private final ContractTicketService tickets;
    private final TransactionRunner transactions;
    private final ContractEventPublisher events;
    private final Clock clock;

    public ContractService(ContractRepository contracts, ContractPlayerStateRepository states,
                           PlayerProfileRepository profiles, ScrollItemRepository scrolls, ItemRepository items,
                           MoneyRepository money, ContractGenerator generator, ContractTicketService tickets,
                           TransactionRunner transactions, ContractEventPublisher events, Clock clock) {
        this.contracts = contracts;
        this.states = states;
        this.profiles = profiles;
        this.scrolls = scrolls;
        this.items = items;
        this.money = money;
        this.generator = generator;
        this.tickets = tickets;
        this.transactions = transactions;
        this.events = events;
        this.clock = clock;
    }

    /** Dépense un ticket pour acheter un parchemin aléatoire. Le ticket n'est pas dépensé si le joueur ne peut pas le recevoir. */
    public PurchaseResult buyScroll(PlayerID player) {
        ContractPlayerState state = tickets.currentState(player);
        if (state.tickets() < 1) {
            return PurchaseResult.failed(PurchaseStatus.NO_TICKET);
        }
        if (!scrolls.canReceive(player)) {
            return PurchaseResult.failed(PurchaseStatus.INVENTORY_FULL);
        }
        PlayerContractProfile profile = profiles.profileOf(player);
        Optional<Contract> draft = generator.generate(player, profile);
        if (draft.isEmpty()) {
            return PurchaseResult.failed(PurchaseStatus.NOTHING_UNLOCKED);
        }
        Contract saved = transactions.inTransaction(() -> {
            Contract contract = contracts.save(draft.get());
            states.save(state.spendOne());
            return contract;
        });
        if (!scrolls.give(player, saved)) {
            transactions.inTransaction(() -> {
                contracts.delete(saved.id());
                states.save(state);
                return null;
            });
            return PurchaseResult.failed(PurchaseStatus.INVENTORY_FULL);
        }
        events.publish(new ContractIssued(player, saved.id()));
        return PurchaseResult.ok(saved);
    }

    /**
     * Donne le parchemin de découverte gratuit à un joueur qui n'en a jamais reçu. Ne coûte pas de ticket.
     *
     * @return vrai si un parchemin a été donné
     */
    public boolean giveDiscoveryScroll(PlayerID player) {
        ContractPlayerState state = tickets.currentState(player);
        if (state.discoveryGiven() || !scrolls.canReceive(player)) {
            return false;
        }
        Optional<Contract> draft = generator.generateDiscovery(player, profiles.profileOf(player));
        if (draft.isEmpty()) {
            return false;
        }
        Contract saved = transactions.inTransaction(() -> {
            Contract contract = contracts.save(draft.get());
            states.save(state.withDiscoveryGiven());
            return contract;
        });
        if (!scrolls.give(player, saved)) {
            transactions.inTransaction(() -> {
                contracts.delete(saved.id());
                states.save(state);
                return null;
            });
            return false;
        }
        events.publish(new ContractIssued(player, saved.id()));
        return true;
    }

    /**
     * Livre un parchemin au PNJ d'un métier, en une seule fois. Toute la récompense va au joueur qui livre.
     * Tout est vérifié d'abord ; les items et le parchemin sont retirés, puis le contrat est supprimé et payé dans une
     * transaction. Si elle échoue, les items et le parchemin sont rendus.
     */
    public DeliveryResult deliver(PlayerID player, long contractId, String npcJob) {
        Optional<Contract> found = contracts.findById(contractId);
        if (found.isEmpty()) {
            return DeliveryResult.failed(DeliveryStatus.INVALID_SCROLL);
        }
        Contract contract = found.get();
        if (!contract.isForJob(npcJob)) {
            return DeliveryResult.failed(DeliveryStatus.WRONG_CLIENT);
        }
        if (contract.isExpired(clock.instant())) {
            transactions.inTransaction(() -> contracts.delete(contractId));
            return DeliveryResult.failed(DeliveryStatus.EXPIRED);
        }
        if (!scrolls.has(player, contractId)) {
            return DeliveryResult.failed(DeliveryStatus.INVALID_SCROLL);
        }
        int have = items.countAtLeast(player, contract.material(), contract.minimalQuality());
        if (have < contract.quantity()) {
            int missing = contract.quantity() - have;
            boolean lowerQualityExists = items.countAtLeast(player, contract.material(), Quality.POOR) > have;
            return DeliveryResult.missing(lowerQualityExists
                    ? DeliveryStatus.NOT_ENOUGH_QUALITY
                    : DeliveryStatus.MISSING_ITEMS, missing);
        }

        if (!scrolls.remove(player, contractId)) {
            return DeliveryResult.failed(DeliveryStatus.INVALID_SCROLL);
        }
        Map<Quality, Integer> removed = items.removeAtLeast(player, contract.material(),
                contract.minimalQuality(), contract.quantity());
        if (removed.isEmpty()) {
            scrolls.give(player, contract);
            return DeliveryResult.missing(DeliveryStatus.MISSING_ITEMS, contract.quantity());
        }

        DeliveryResult result;
        try {
            result = transactions.inTransaction(() -> payAndDelete(player, contract));
        } catch (PaymentFailedException e) {
            giveBack(player, contract, removed);
            return DeliveryResult.failed(DeliveryStatus.PAYMENT_FAILED);
        } catch (RuntimeException e) {
            giveBack(player, contract, removed);
            throw e;
        }
        if (!result.isDelivered()) {
            giveBack(player, contract, removed);
            return result;
        }
        events.publish(new ContractDelivered(player, contract.issuedTo(), contractId, contract.type(),
                contract.material(), contract.quantity(), contract.reward()));
        return result;
    }

    private DeliveryResult payAndDelete(PlayerID player, Contract contract) {
        if (!contracts.delete(contract.id())) {
            return DeliveryResult.failed(DeliveryStatus.INVALID_SCROLL);
        }
        if (!money.pay(player, contract.reward(), REWARD_REASON)) {
            throw new PaymentFailedException();
        }
        states.save(states.find(player).withOneMoreDelivered());
        return DeliveryResult.delivered(contract.reward());
    }

    private void giveBack(PlayerID player, Contract contract, Map<Quality, Integer> removed) {
        items.giveBack(player, contract.material(), removed);
        scrolls.give(player, contract);
    }

    public boolean isValid(long contractId) {
        return contracts.findById(contractId).map(c -> !c.isExpired(clock.instant())).orElse(false);
    }

    public Optional<Contract> find(long contractId) {
        return contracts.findById(contractId);
    }

    public int purgeExpired() {
        List<Long> ids = transactions.inTransaction(() -> contracts.deleteExpired(clock.instant()));
        ids.forEach(id -> events.publish(new ContractExpired(id)));
        return ids.size();
    }

    private static final class PaymentFailedException extends RuntimeException {
        PaymentFailedException() {
            super("payment failed", null, false, false);
        }
    }
}