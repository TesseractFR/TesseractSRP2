package onl.tesseract.srp.commission.domain.port.serverside;

import onl.tesseract.srp.commission.domain.model.board.BoardId;
import onl.tesseract.srp.commission.domain.model.board.BoardLevels;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.GuildId;

import java.util.Optional;

/** Port de persistance des tableaux de commissions. */
public interface CommissionBoardRepository {
    /** Enregistre le tableau du spawn avec ces niveaux, sans taxe ni commission terminée. */
    CommissionBoard createSpawnBoard(BoardLevels levels);

    /** Enregistre le tableau d'une guilde avec les niveaux {@link BoardLevels#GUILD_START} et cette taxe, pas encore posé. */
    CommissionBoard createGuildBoard(GuildId guildId, double usageTaxRate);

    /** Enregistre les niveaux, la taxe et la position du tableau. */
    void save(CommissionBoard commissionBoard);

    /** @return le tableau, ou {@link Optional#empty()} s'il n'existe pas */
    Optional<CommissionBoard> findById(BoardId boardId);

    /** @return le tableau du spawn, ou {@link Optional#empty()} s'il n'a pas encore été créé */
    Optional<CommissionBoard> findSpawnBoard();

    /** @return le tableau de la guilde, ou {@link Optional#empty()} s'il n'a pas encore été créé */
    Optional<CommissionBoard> findByGuild(GuildId guildId);

    /** Supprime le tableau de la guilde et ses commissions. Ne fait rien s'il n'existe pas. */
    void deleteByGuild(GuildId guildId);
}
