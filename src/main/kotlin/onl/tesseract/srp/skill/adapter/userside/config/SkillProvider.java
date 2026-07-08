package onl.tesseract.srp.skill.adapter.userside.config;

import onl.tesseract.srp.repository.yaml.skill.SkillConfigRepository;
import onl.tesseract.srp.skill.domain.port.serverside.*;
import onl.tesseract.srp.skill.domain.port.userside.CraftingService;
import onl.tesseract.srp.skill.domain.port.userside.SkillService;
import onl.tesseract.srp.skill.domain.port.userside.StationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SkillProvider {

    @Bean
    public SkillService skillService(SkillConfigRepository skillConfigRepository) {
        return new SkillService(skillConfigRepository);
    }

    @Bean
    public CraftingService craftingService(SkillResultCacheRepository skillResultCacheRepository,
                                           CraftingRepository craftingRepository, ItemRepository itemRepository,
                                           CraftTaskScheduler craftTaskScheduler, MessagingRepository messagingRepository) {
        return new CraftingService(skillResultCacheRepository, craftingRepository, itemRepository,
                craftTaskScheduler, messagingRepository);
    }

    @Bean
    public StationService stationService(TerritoryRepository territoryRepository, StationRepository stationRepository){
        return new StationService(territoryRepository,stationRepository);
    }
}
