package onl.tesseract.srp.territory.adapter.userside.config;

import onl.tesseract.srp.common.domain.port.userside.plugin.EventPublisher;
import onl.tesseract.srp.territory.adapter.serverside.repository.*;
import onl.tesseract.srp.territory.adapter.serverside.scheduler.TerritoryBorderTaskSchedulerImpl;
import onl.tesseract.srp.territory.adapter.serverside.service.MoneyServiceImpl;
import onl.tesseract.srp.territory.domain.port.userside.campement.CampementService;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildBorderService;
import onl.tesseract.srp.territory.domain.port.userside.campement.CampementBorderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TerritoryProvider {

    @Bean
    public GuildService guildService(
            GuildRepositoryImpl guildRepository,
            TerritoryChunkRepositoryImpl territoryChunkRepository,
            SrpPlayerRepositoryImpl srpPlayerService,
            EventPublisher eventService,
            MoneyServiceImpl moneyService) {
        return new GuildService(guildRepository, territoryChunkRepository, srpPlayerService, eventService, moneyService);
    }

    @Bean
    public CampementService campementService(
            CampementRepositoryImpl campementRepository,
            TerritoryChunkRepositoryImpl territoryChunkRepository,
            EventPublisher eventService,
            SrpPlayerRepositoryImpl srpPlayerService) {
        return new CampementService(campementRepository, territoryChunkRepository, eventService, srpPlayerService);
    }

    @Bean
    public GuildBorderService guildTerritoryBorderService(
            TerritoryBorderTaskSchedulerImpl scheduler,
            GuildService guildService) {
        return new GuildBorderService(scheduler, guildService);
    }

    @Bean
    public CampementBorderService campementTerritoryBorderService(
            TerritoryBorderTaskSchedulerImpl scheduler,
            CampementService campementService) {
        return new CampementBorderService(scheduler, campementService);
    }
}

