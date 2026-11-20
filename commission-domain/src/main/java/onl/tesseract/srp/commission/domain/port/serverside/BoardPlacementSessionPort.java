package onl.tesseract.srp.commission.domain.port.serverside;

import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.common.domain.model.PlayerId;

import java.util.Optional;

/**
 * Port de suivi des joueurs qui ont lancé la pose du tableau de leur guilde et doivent encore choisir
 * son emplacement. Une session se termine seule après {@link CommissionBoard#BOARD_PLACEMENT_SECONDS} secondes.
 */
public interface BoardPlacementSessionPort {
    /** Démarre une session pour le joueur, en remplaçant celle qu'il avait déjà. */
    void startSession(PlayerId player, GuildId guildId);

    /** @return la guilde dont le joueur pose le tableau, ou {@link Optional#empty()} s'il n'en pose pas */
    Optional<GuildId> findGuildOfPlacingPlayer(PlayerId player);

    /** @return {@code false} si le joueur n'avait pas de session */
    boolean endSession(PlayerId player);
}
