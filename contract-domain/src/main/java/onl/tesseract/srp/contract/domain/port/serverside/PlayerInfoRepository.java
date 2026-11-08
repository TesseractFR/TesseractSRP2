package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;

import java.util.OptionalInt;

/** Port d'accès aux tickets, quotas et bonus associés aux joueurs. */
public interface PlayerInfoRepository {
    /** Recherche le solde enregistré, s'il existe. */
    OptionalInt findTickets(PlayerId player);
    /** Enregistre le solde de tickets du joueur. */
    void saveTickets(PlayerId player, int tickets);
    /** Retourne le quota quotidien de tickets du joueur. */
    int getDailyQuota(PlayerId player);
    /** Retourne le taux de bonus lié au métier et au matériau pour le joueur. */
    double getMoneyBonus(PlayerId player, String job, String material);
}
