package onl.tesseract.srp.domain.territory

abstract class TerritoryChunk(val chunkCoord: ChunkCoord) {
    override fun toString(): String = chunkCoord.toString()
    abstract fun getOwner(): Territory<out TerritoryChunk>
}
