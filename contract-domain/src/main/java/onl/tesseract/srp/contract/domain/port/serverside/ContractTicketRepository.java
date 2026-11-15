package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;

import java.util.OptionalInt;

/** Port de persistance du solde de tickets de contrat des joueurs. */
public interface ContractTicketRepository {
    /** @return le solde enregistré, ou un résultat vide si le joueur n'en a jamais eu */
    OptionalInt findTickets(PlayerId player);

    /** Enregistre le solde de tickets du joueur. */
    void saveTickets(PlayerId player, int tickets);
}
