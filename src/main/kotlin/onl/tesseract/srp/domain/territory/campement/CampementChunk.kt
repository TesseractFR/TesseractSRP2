package onl.tesseract.srp.domain.territory.campement

import onl.tesseract.srp.domain.commun.ChunkCoord
import onl.tesseract.srp.domain.territory.TerritoryChunk

class CampementChunk(chunkCoord: ChunkCoord, val campement: Campement) : TerritoryChunk(chunkCoord){
    override fun getOwner(): Campement {
        return campement
    }
}
