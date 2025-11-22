package onl.tesseract.srp.domain.territory

data class Coordinate(
    val x : Double,
    val y : Double,
    val z : Double,
    val chunkCoord: ChunkCoord
)