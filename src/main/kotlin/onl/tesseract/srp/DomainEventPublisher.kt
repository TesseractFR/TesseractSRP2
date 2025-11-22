package onl.tesseract.srp

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class DomainEventPublisher(
    private val publisher: ApplicationEventPublisher
) {
    fun publish(event: Any) = publisher.publishEvent(event)
}