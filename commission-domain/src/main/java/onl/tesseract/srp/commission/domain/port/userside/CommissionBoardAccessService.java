package onl.tesseract.srp.commission.domain.port.userside;

import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardPart;
import onl.tesseract.srp.commission.domain.port.serverside.GuildMembershipPort;
import onl.tesseract.srp.common.domain.model.PlayerId;

import java.util.List;

/**
 * Relie un joueur à la guilde propriétaire d'un tableau : ce qu'il y voit selon qu'il en est membre ou non,
 * et qui sont les membres à prévenir de ce que le tableau rapporte à la guilde.
 */
public class CommissionBoardAccessService {
    private final GuildMembershipPort guildMembershipPort;

    public CommissionBoardAccessService(GuildMembershipPort guildMembershipPort) {
        this.guildMembershipPort = guildMembershipPort;
    }

    /** @return {@code true} si le joueur voit cette partie : la partie publique est ouverte à tous */
    public boolean canAccessPart(PlayerId player, CommissionBoard commissionBoard, CommissionBoardPart boardPart) {
        return boardPart == CommissionBoardPart.PUBLIC || isGuildMember(player, commissionBoard);
    }

    /** @return {@code true} si le joueur est membre de la guilde propriétaire ; toujours faux au spawn */
    public boolean isGuildMember(PlayerId player, CommissionBoard commissionBoard) {
        return commissionBoard.findGuildId()
                .map(guildId -> guildMembershipPort.isMember(guildId, player))
                .orElse(false);
    }

    /** @return les membres de la guilde propriétaire, à prévenir des gains de la guilde ; liste vide au spawn */
    public List<PlayerId> findAllGuildMembers(CommissionBoard commissionBoard) {
        return commissionBoard.findGuildId()
                .map(guildMembershipPort::findAllMembers)
                .orElse(List.of());
    }
}
