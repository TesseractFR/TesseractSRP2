package onl.tesseract.srp.service.equipment.annexionStick

import onl.tesseract.lib.equipment.EquipmentService
import onl.tesseract.srp.DomainEventPublisher
import onl.tesseract.srp.domain.equipment.annexionStick.event.AnnexionStickGivenEvent
import onl.tesseract.srp.util.equipment.annexionStick.AnnexionStickInvocable
import org.springframework.stereotype.Service
import kotlin.reflect.KClass
import java.util.UUID

@Service
class AnnexionStickService(
    private val equipmentService: EquipmentService,
    private val eventPublisher: DomainEventPublisher
) {
    fun <T : AnnexionStickInvocable> giveStick(
        playerId: UUID,
        invocableType: KClass<T>,
        factory: (UUID) -> T
    ) {
        val equipment = equipmentService.getEquipment(playerId)
        equipment.get(invocableType.java) ?: factory(playerId).also {
            equipmentService.add(playerId, it)
        }
        eventPublisher.publish(AnnexionStickGivenEvent(playerId,invocableType.java))
    }
}
