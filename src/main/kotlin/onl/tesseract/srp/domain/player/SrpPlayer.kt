package onl.tesseract.srp.domain.player

import java.util.*

class SrpPlayer(
    val uniqueId: UUID,
    var rank: PlayerRank = PlayerRank.Survivant,
)