package onl.tesseract.srp.domain.equipment.elytra.event

import java.util.UUID

data class ElytraPropulsionRequestedEvent(
    val playerId: UUID
)