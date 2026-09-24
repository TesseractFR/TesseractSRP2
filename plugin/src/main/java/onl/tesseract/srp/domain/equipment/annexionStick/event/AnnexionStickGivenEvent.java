package onl.tesseract.srp.domain.equipment.annexionStick.event;

import java.util.UUID;

/**
 * Event published when an annexation stick is given to a player.
 */
public record AnnexionStickGivenEvent(UUID playerId, Class<?> invocableType) {
}

