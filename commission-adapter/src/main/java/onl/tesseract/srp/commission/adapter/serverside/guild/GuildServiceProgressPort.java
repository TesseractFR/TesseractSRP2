package onl.tesseract.srp.commission.adapter.serverside.guild;

import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.commission.domain.port.serverside.GuildProgressPort;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
import org.springframework.stereotype.Component;

/** Lit le niveau des guildes et débite leur banque au moyen de {@link GuildService}. */
@Component
public class GuildServiceProgressPort implements GuildProgressPort {

    private final GuildService guildService;

    public GuildServiceProgressPort(GuildService guildService) {
        this.guildService = guildService;
    }

    @Override
    public int getGuildLevel(GuildId guildId) {
        return guildService.getGuildLevel(guildId.value());
    }

    @Override
    public int getBankBalance(GuildId guildId) {
        return guildService.getGuildMoney(guildId.value());
    }

    @Override
    public boolean withdrawFromBank(GuildId guildId, int amount) {
        return guildService.payFromBank(guildId.value(), amount);
    }
}
