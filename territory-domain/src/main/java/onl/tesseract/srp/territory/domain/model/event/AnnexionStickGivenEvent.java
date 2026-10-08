package onl.tesseract.srp.territory.domain.model.event;

import java.util.UUID;

/**
 * Event published when an annexation stick is given to a player.
 */
public record AnnexionStickGivenEvent(UUID playerId, Class<?> invocableType) {
}

