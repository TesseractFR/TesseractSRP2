package onl.tesseract.srp.contract.domain.port.userside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.model.exception.InvalidTicketAmountException;

/** Cas d'usage de gestion des tickets de contrat. */
public interface ContractTicketUseCase {
    /** @return solde de tickets du joueur, ou son quota quotidien s'il n'a jamais eu de solde */
    int getTickets(PlayerId player);

    /** @return quota quotidien de tickets du joueur */
    int getDailyQuota(PlayerId player);

    /**
     * Ajoute des tickets au solde d'un joueur, à la demande du staff.
     *
     * @throws InvalidTicketAmountException si le nombre n'est pas strictement positif
     */
    void giveTickets(PlayerId target, int amount);

    /** Remet le solde du joueur à son quota quotidien. */
    void renewDaily(PlayerId player);
}
