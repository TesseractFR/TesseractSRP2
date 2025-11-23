package onl.tesseract.srp.domain.territory

import onl.tesseract.srp.domain.commun.ChunkCoord

abstract class TerritoryChunk(val chunkCoord: ChunkCoord) {
    override fun toString(): String = chunkCoord.toString()
    abstract fun getOwner(): Territory<out TerritoryChunk>
    override fun equals(other: Any?): Boolean {
        if(other==null || other::class != this::class)return false
        other as TerritoryChunk
        return this.getOwner() == other.getOwner() && this.chunkCoord == other.chunkCoord
    }
}
