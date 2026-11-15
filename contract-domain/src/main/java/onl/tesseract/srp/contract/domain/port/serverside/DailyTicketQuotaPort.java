package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;

/** Port de calcul du quota quotidien de tickets d'un joueur. */
public interface DailyTicketQuotaPort {
    /** @return quota quotidien du joueur, zéro s'il n'y a droit à aucun ticket */
    int getDailyQuota(PlayerId player);
}
