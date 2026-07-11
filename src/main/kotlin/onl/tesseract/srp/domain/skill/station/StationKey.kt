package onl.tesseract.srp.domain.skill.station

import java.util.UUID

data class StationKey(
    val skillName: String,
    val territory: UUID
)
