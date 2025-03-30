package onl.tesseract.srp

import onl.tesseract.lib.chat.ChatEntryService
import onl.tesseract.lib.equipment.EquipmentService
import onl.tesseract.lib.event.EventService
import onl.tesseract.lib.menu.MenuService
import onl.tesseract.lib.persistantcontainer.NamedspacedKeyProvider
import onl.tesseract.lib.profile.PlayerProfileService
import onl.tesseract.lib.service.ServiceContainer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class SrpServiceBeans {

    @Bean
    open fun namedSpacedKeyProvider(): NamedspacedKeyProvider {
        return ServiceContainer[NamedspacedKeyProvider::class.java]
    }

    @Bean
    open fun eventService(): EventService {
        return ServiceContainer[EventService::class.java]
    }

    @Bean
    open fun chatEntryService(): ChatEntryService {
        return ServiceContainer[ChatEntryService::class.java]
    }

    @Bean
    open fun equipmentService(): EquipmentService {
        return ServiceContainer[EquipmentService::class.java]
    }

    @Bean
    open fun menuService(): MenuService {
        return ServiceContainer[MenuService::class.java]
    }

    @Bean
    open fun playerProfileService(): PlayerProfileService {
        return ServiceContainer[PlayerProfileService::class.java]
    }
}