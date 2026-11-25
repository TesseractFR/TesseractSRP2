package onl.tesseract.srp.commission.domain.port.serverside;

import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.common.domain.model.PlayerId;

import java.util.List;

/** Port d'accès à l'appartenance et au rôle des joueurs dans les guildes. */
public interface GuildMembershipPort {
    /** @return {@code true} si le joueur est membre de la guilde */
    boolean isMember(GuildId guildId, PlayerId player);

    /** @return {@code true} si le joueur est membre de la guilde avec un rôle qui gère le tableau (Adjoint ou chef) */
    boolean canManageBoard(GuildId guildId, PlayerId player);

    /** @return les membres de la guilde, liste vide si elle n'en a aucun */
    List<PlayerId> findAllMembers(GuildId guildId);
}
