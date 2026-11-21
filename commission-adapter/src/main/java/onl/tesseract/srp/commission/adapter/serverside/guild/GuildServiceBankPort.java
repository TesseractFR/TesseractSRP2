package onl.tesseract.srp.commission.adapter.serverside.guild;

import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.commission.domain.port.serverside.GuildBankPort;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
import org.springframework.stereotype.Component;

/** Lit et débite la banque des guildes au moyen de {@link GuildService}. */
@Component
public class GuildServiceBankPort implements GuildBankPort {

    private final GuildService guildService;

    public GuildServiceBankPort(GuildService guildService) {
        this.guildService = guildService;
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
