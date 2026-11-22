package onl.tesseract.srp.player.adapter.userside.config;

import onl.tesseract.srp.common.domain.port.userside.plugin.EventPublisher;
import onl.tesseract.srp.player.domain.port.serverside.PlayerLedgerPort;
import onl.tesseract.srp.player.domain.port.serverside.PlayerRepository;
import onl.tesseract.srp.player.domain.port.userside.PlayerIlluminationService;
import onl.tesseract.srp.player.domain.port.userside.PlayerMoneyService;
import onl.tesseract.srp.player.domain.port.userside.PlayerRankService;
import onl.tesseract.srp.player.domain.port.userside.SrpPlayerProfileService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Assemble les services du domaine joueur, un par cas d'usage. */
@Configuration
public class PlayerProvider {

    @Bean
    public SrpPlayerProfileService srpPlayerProfileService(PlayerRepository playerRepository) {
        return new SrpPlayerProfileService(playerRepository);
    }

    @Bean
    public PlayerRankService playerRankService(SrpPlayerProfileService srpPlayerProfileService,
                                               PlayerLedgerPort playerLedgerPort,
                                               EventPublisher eventPublisher) {
        return new PlayerRankService(srpPlayerProfileService, playerLedgerPort, eventPublisher);
    }

    @Bean
    public PlayerMoneyService playerMoneyService(SrpPlayerProfileService srpPlayerProfileService,
                                                 PlayerLedgerPort playerLedgerPort) {
        return new PlayerMoneyService(srpPlayerProfileService, playerLedgerPort);
    }

    @Bean
    public PlayerIlluminationService playerIlluminationService(SrpPlayerProfileService srpPlayerProfileService) {
        return new PlayerIlluminationService(srpPlayerProfileService);
    }
}
