package onl.tesseract.srp.contract.adapter.serverside.ticket;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.port.userside.PlayerService;
import onl.tesseract.srp.contract.adapter.serverside.yaml.DailyTicketConfig;
import onl.tesseract.srp.contract.domain.port.serverside.DailyTicketQuotaPort;
import org.springframework.stereotype.Component;

/** Détermine le quota quotidien selon le grade du joueur et la table {@code daily-tickets} de la configuration. */
@Component
public class YamlDailyTicketQuotaPort implements DailyTicketQuotaPort {

    private final PlayerService playerService;
    private final DailyTicketConfig dailyTicketConfig;

    public YamlDailyTicketQuotaPort(PlayerService playerService, DailyTicketConfig dailyTicketConfig) {
        this.playerService = playerService;
        this.dailyTicketConfig = dailyTicketConfig;
    }

    /** Un joueur sans grade n'a droit à aucun ticket. */
    @Override
    public int getDailyQuota(PlayerId player) {
        return java.util.Optional.ofNullable(playerService.getPlayer(player).getRank())
                .map(rank -> dailyTicketConfig.getDailyTickets(rank.name()))
                .orElse(0);
    }
}
