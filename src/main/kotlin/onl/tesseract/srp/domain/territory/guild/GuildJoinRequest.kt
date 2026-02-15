package onl.tesseract.srp.domain.territory.guild

import java.time.Instant
import java.util.UUID

data class GuildJoinRequest(
    val id: UUID?,
    val playerID: UUID,
    val message: String,
    val requestedDate: Instant
)
