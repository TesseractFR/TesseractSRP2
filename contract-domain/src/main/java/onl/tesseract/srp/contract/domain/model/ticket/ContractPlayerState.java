package onl.tesseract.srp.contract.domain.model.ticket;

import onl.tesseract.srp.contract.domain.model.PlayerID;

import java.time.LocalDate;


public record ContractPlayerState(PlayerID playerId, int tickets, LocalDate ticketsDay,
                                  int deliveredCount, boolean discoveryGiven) {

    public ContractPlayerState {
        if (playerId == null) throw new IllegalArgumentException("playerId");
        if (tickets < 0) throw new IllegalArgumentException("tickets must be >= 0");
    }

    public static ContractPlayerState fresh(PlayerID playerId) {
        return new ContractPlayerState(playerId, 0, null, 0, false);
    }

    public ContractPlayerState renewedFor(LocalDate today, int dailyQuota) {
        if (ticketsDay != null && !ticketsDay.isBefore(today)) {
            return this;
        }
        return new ContractPlayerState(playerId, dailyQuota, today, deliveredCount, discoveryGiven);
    }

    public ContractPlayerState spendOne() {
        if (tickets < 1) throw new IllegalStateException("no ticket left");
        return withTickets(tickets - 1);
    }

    public ContractPlayerState withTickets(int newTickets) {
        return new ContractPlayerState(playerId, newTickets, ticketsDay, deliveredCount, discoveryGiven);
    }

    public ContractPlayerState withOneMoreDelivered() {
        return new ContractPlayerState(playerId, tickets, ticketsDay, deliveredCount + 1, discoveryGiven);
    }

    public ContractPlayerState withDiscoveryGiven() {
        return new ContractPlayerState(playerId, tickets, ticketsDay, deliveredCount, true);
    }
}
