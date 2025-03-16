package onl.tesseract.srp

import onl.tesseract.lib.event.EventService
import onl.tesseract.lib.persistantcontainer.NamedspacedKeyProvider
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
}