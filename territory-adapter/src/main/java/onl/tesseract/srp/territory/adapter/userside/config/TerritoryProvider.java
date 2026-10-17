package onl.tesseract.srp.territory.adapter.userside.config;

import onl.tesseract.srp.common.domain.port.userside.plugin.EventPublisher;
import onl.tesseract.srp.territory.adapter.serverside.repository.*;
import onl.tesseract.srp.territory.adapter.serverside.scheduler.TerritoryBorderTaskSchedulerImpl;
import onl.tesseract.srp.territory.adapter.serverside.service.MoneyServiceImpl;
import onl.tesseract.srp.territory.domain.port.serverside.PlayerCampementMessagingRepository;
import onl.tesseract.srp.territory.domain.port.serverside.PlayerGuildMessagingRepository;
import onl.tesseract.srp.territory.domain.port.userside.campement.CampementService;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildBorderService;
import onl.tesseract.srp.territory.domain.port.userside.campement.CampementBorderService;
import onl.tesseract.lib.service.ServiceContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.SmartInitializingSingleton;

@Configuration
public class TerritoryProvider {

    @Bean
    public GuildService guildService(
            GuildRepositoryImpl guildRepository,
            TerritoryChunkRepositoryImpl territoryChunkRepository,
            SrpPlayerRepositoryImpl srpPlayerService,
            EventPublisher eventService,
            MoneyServiceImpl moneyService,
            PlayerGuildMessagingRepository messaging) {
        return new GuildService(guildRepository, territoryChunkRepository, srpPlayerService, eventService, moneyService, messaging);
    }

    @Bean
    public CampementService campementService(
            CampementRepositoryImpl campementRepository,
            TerritoryChunkRepositoryImpl territoryChunkRepository,
            EventPublisher eventService,
            SrpPlayerRepositoryImpl srpPlayerService,
            PlayerCampementMessagingRepository messaging) {
        return new CampementService(campementRepository, territoryChunkRepository, eventService, srpPlayerService, messaging);
    }

    @Bean
    public GuildBorderService guildTerritoryBorderService(
            TerritoryBorderTaskSchedulerImpl scheduler,
            GuildService guildService,
            PlayerGuildMessagingRepository messaging) {
        return new GuildBorderService(scheduler, guildService, messaging);
    }

    @Bean
    public CampementBorderService campementTerritoryBorderService(
            TerritoryBorderTaskSchedulerImpl scheduler,
            CampementService campementService,
            PlayerCampementMessagingRepository messaging) {
        return new CampementBorderService(scheduler, campementService, messaging);
    }

    @Bean
    public SmartInitializingSingleton registerTerritoryServices(
            GuildService guildService,
            CampementService campementService) {
        return () -> {
            ServiceContainer.getInstance().registerService(GuildService.class, guildService);
            ServiceContainer.getInstance().registerService(CampementService.class, campementService);
        };
    }
}

