package onl.tesseract.srp.domain.territory.guild

import onl.tesseract.srp.domain.commun.ChunkCoord
import onl.tesseract.srp.domain.territory.TerritoryChunk

class GuildChunk(chunkCoord: ChunkCoord, val guild: Guild) : TerritoryChunk(chunkCoord) {

    override fun getOwner(): Guild {
        return guild
    }
}
