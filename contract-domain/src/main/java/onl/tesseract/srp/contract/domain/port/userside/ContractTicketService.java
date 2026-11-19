package onl.tesseract.srp.contract.domain.port.userside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.model.exception.InvalidTicketAmountException;
import onl.tesseract.srp.contract.domain.port.serverside.ContractTicketRepository;
import onl.tesseract.srp.contract.domain.port.serverside.DailyTicketQuotaPort;

/** Gère le solde de tickets de contrat des joueurs : consultation, dépense, attribution et renouvellement. */
public class ContractTicketService {

    private final ContractTicketRepository contractTicketRepository;
    private final DailyTicketQuotaPort dailyTicketQuotaPort;

    public ContractTicketService(ContractTicketRepository contractTicketRepository,
                                 DailyTicketQuotaPort dailyTicketQuotaPort) {
        this.contractTicketRepository = contractTicketRepository;
        this.dailyTicketQuotaPort = dailyTicketQuotaPort;
    }

    /** @return solde de tickets du joueur, ou son quota quotidien s'il n'a jamais eu de solde */
    public int getTickets(PlayerId player) {
        return contractTicketRepository.findTickets(player).orElseGet(() -> getDailyQuota(player));
    }

    /** @return quota quotidien de tickets du joueur */
    public int getDailyQuota(PlayerId player) {
        return dailyTicketQuotaPort.getDailyQuota(player);
    }

    /** @return {@code true} si le joueur a au moins un ticket */
    public boolean hasTicket(PlayerId player) {
        return getTickets(player) >= 1;
    }

    /** Dépense un ticket du joueur. */
    public void withdrawTicket(PlayerId player) {
        contractTicketRepository.saveTickets(player, getTickets(player) - 1);
    }

    /** Rend au joueur un ticket dépensé pour un achat qui a échoué. */
    public void refundTicket(PlayerId player) {
        addTickets(player, 1);
    }

    /**
     * Ajoute des tickets au solde d'un joueur, à la demande du staff, sans plafond.
     *
     * @throws InvalidTicketAmountException si le nombre n'est pas strictement positif
     */
    public void giveTickets(PlayerId target, int amount) {
        if (amount <= 0) {
            throw new InvalidTicketAmountException(amount);
        }
        addTickets(target, amount);
    }

    /** Remet le solde du joueur à son quota quotidien. */
    public void renewDaily(PlayerId player) {
        int quota = getDailyQuota(player);
        if (getTickets(player) != quota) {
            contractTicketRepository.saveTickets(player, quota);
        }
    }

    private void addTickets(PlayerId player, int amount) {
        contractTicketRepository.saveTickets(player, getTickets(player) + amount);
    }
}
