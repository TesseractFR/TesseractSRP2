package onl.tesseract.srp.commission.adapter.serverside.guild;

import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.commission.domain.port.serverside.GuildMembershipPort;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.territory.domain.model.guild.Guild;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/** Lit l'appartenance et le rôle des joueurs au moyen de {@link GuildService}. */
@Component
public class GuildServiceMembershipPort implements GuildMembershipPort {

    private final GuildService guildService;

    public GuildServiceMembershipPort(GuildService guildService) {
        this.guildService = guildService;
    }

    @Override
    public boolean isMember(GuildId guildId, PlayerId player) {
        return Optional.ofNullable(guildService.getGuildByMember(player.value()))
                .map(Guild::getId)
                .filter(guildId.value()::equals)
                .isPresent();
    }

    @Override
    public boolean canManageBoard(GuildId guildId, PlayerId player) {
        return isMember(guildId, player) && guildService.getMemberRole(player.value()).canManageBoard();
    }

    @Override
    public List<PlayerId> findAllMembers(GuildId guildId) {
        return guildService.getMemberIds(guildId.value()).stream().map(PlayerId::new).toList();
    }
}
