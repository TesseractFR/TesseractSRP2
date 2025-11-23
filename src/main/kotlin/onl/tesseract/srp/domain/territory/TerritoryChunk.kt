package onl.tesseract.srp.domain.territory

import onl.tesseract.srp.domain.commun.ChunkCoord

abstract class TerritoryChunk(val chunkCoord: ChunkCoord) {
    override fun toString(): String = chunkCoord.toString()
    abstract fun getOwner(): Territory<out TerritoryChunk>
}
