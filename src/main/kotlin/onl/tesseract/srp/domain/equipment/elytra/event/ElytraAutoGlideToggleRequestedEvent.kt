package onl.tesseract.srp.domain.equipment.elytra.event

import java.util.UUID

data class ElytraAutoGlideToggleRequestedEvent(
    val playerId: UUID
)
