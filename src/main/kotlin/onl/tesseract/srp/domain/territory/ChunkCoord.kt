package onl.tesseract.srp.domain.territory

import org.bukkit.Chunk
import org.bukkit.Location

data class ChunkCoord(val x: Int, val z: Int, val world: String){
    constructor(location: Location): this(location.chunk.x, location.chunk.z, location.world.name)
    constructor(chunk: Chunk): this(chunk.x, chunk.z, chunk.world.name)
    override fun toString(): String = "($x, $z, $world)"
}
