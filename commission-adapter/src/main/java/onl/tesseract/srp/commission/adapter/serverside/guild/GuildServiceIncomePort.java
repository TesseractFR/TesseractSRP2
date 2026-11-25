package onl.tesseract.srp.commission.adapter.serverside.guild;

import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.commission.domain.port.serverside.GuildIncomePort;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
import org.springframework.stereotype.Component;

/** Verse aux guildes la taxe et l'XP des livraisons au moyen de {@link GuildService}. */
@Component
public class GuildServiceIncomePort implements GuildIncomePort {

    private final GuildService guildService;

    public GuildServiceIncomePort(GuildService guildService) {
        this.guildService = guildService;
    }

    @Override
    public void addCommissionIncome(GuildId guildId, int usageTax, int xp) {
        guildService.addCommissionIncome(guildId.value(), usageTax, xp);
    }
}
