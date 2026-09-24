package onl.tesseract.srp;

import onl.tesseract.core.boutique.BoutiqueService;
import onl.tesseract.core.persistence.hibernate.boutique.TPlayerInfoService;
import onl.tesseract.core.title.TitleService;
import onl.tesseract.lib.chat.ChatEntryService;
import onl.tesseract.lib.equipment.EquipmentService;
import onl.tesseract.lib.event.EventService;
import onl.tesseract.lib.menu.MenuService;
import onl.tesseract.lib.persistantcontainer.NamedspacedKeyProvider;
import onl.tesseract.lib.profile.PlayerProfileService;
import onl.tesseract.lib.service.ServiceContainer;
import onl.tesseract.lib.task.TaskScheduler;
import org.bukkit.plugin.Plugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SrpServiceBeans {

    @Bean
    public NamedspacedKeyProvider namedSpacedKeyProvider() {
        return ServiceContainer.getInstance().get(NamedspacedKeyProvider.class);
    }

    @Bean
    public EventService eventService() {
        return ServiceContainer.getInstance().get(EventService.class);
    }

    @Bean
    public ChatEntryService chatEntryService() {
        return ServiceContainer.getInstance().get(ChatEntryService.class);
    }

    @Bean
    public EquipmentService equipmentService() {
        return ServiceContainer.getInstance().get(EquipmentService.class);
    }

    @Bean
    public MenuService menuService() {
        return ServiceContainer.getInstance().get(MenuService.class);
    }

    @Bean
    public PlayerProfileService playerProfileService() {
        return ServiceContainer.getInstance().get(PlayerProfileService.class);
    }

    @Bean
    public TPlayerInfoService playerInfoService() {
        return ServiceContainer.getInstance().get(TPlayerInfoService.class);
    }

    @Bean
    public TitleService titleService() {
        return ServiceContainer.getInstance().get(TitleService.class);
    }

    @Bean
    public TaskScheduler taskService() {
        return ServiceContainer.getInstance().get(TaskScheduler.class);
    }

    @Bean
    public BoutiqueService boutiqueService() {
        return ServiceContainer.getInstance().get(BoutiqueService.class);
    }

    @Bean
    public Plugin plugin() {
        return TesseractSRP.PLUGIN_INSTANCE;
    }
}

