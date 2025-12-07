package onl.tesseract.srp.mapper

import onl.tesseract.srp.domain.commun.ChunkCoord
import org.bukkit.Chunk
import org.bukkit.Location


fun Location.toChunkCoord() : ChunkCoord{
    return ChunkCoord(this.chunk.x,this.chunk.z,this.world.name)
}

fun Chunk.toChunkCoord() : ChunkCoord{
    return ChunkCoord(this.x,this.z,this.world.name)
}
