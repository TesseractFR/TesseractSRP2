package onl.tesseract.srp.service.equipment.annexionStick;

import onl.tesseract.lib.equipment.EquipmentService;
import onl.tesseract.srp.common.adapter.DomainEventPublisher;
import onl.tesseract.srp.domain.equipment.annexionStick.event.AnnexionStickGivenEvent;
import onl.tesseract.srp.util.equipment.annexionStick.AnnexionStickInvocable;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.function.Function;

/**
 * Service for giving annexation sticks to players.
 */
@Service
public class AnnexionStickService {
    private final EquipmentService equipmentService;
    private final DomainEventPublisher eventPublisher;

    public AnnexionStickService(EquipmentService equipmentService, DomainEventPublisher eventPublisher) {
        this.equipmentService = equipmentService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Gives an annexation stick to a player if they don't already have one.
     */
    public <T extends AnnexionStickInvocable> void giveStick(
            UUID playerId,
            Class<T> invocableType,
            Function<UUID, T> factory
    ) {
        var equipment = equipmentService.getEquipment(playerId);
        var existing = equipment.get(invocableType);
        if (existing == null) {
            var newStick = factory.apply(playerId);
            equipmentService.add(playerId, newStick);
        }
        eventPublisher.publish(new AnnexionStickGivenEvent(playerId, invocableType));
    }
}

