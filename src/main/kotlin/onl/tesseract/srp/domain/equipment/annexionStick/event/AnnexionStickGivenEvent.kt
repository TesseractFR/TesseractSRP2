package onl.tesseract.srp.domain.equipment.annexionStick.event

import onl.tesseract.srp.util.equipment.annexionStick.AnnexionStickInvocable
import java.util.UUID

data class AnnexionStickGivenEvent(
    val playerId: UUID,
    val invocableType: Class<out AnnexionStickInvocable>
)
