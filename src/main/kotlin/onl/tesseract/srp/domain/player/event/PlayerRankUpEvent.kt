package onl.tesseract.srp.domain.player.event

import onl.tesseract.srp.domain.player.PlayerRank
import java.util.UUID

class PlayerRankUpEvent(
    val playerId: UUID,
    val newRank: PlayerRank
)