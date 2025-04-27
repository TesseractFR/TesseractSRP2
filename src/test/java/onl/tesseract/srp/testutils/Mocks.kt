package onl.tesseract.srp.testutils

import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World
import org.mockito.Mockito.*

fun mockWorld(
    name: String = "mockWorld",
    spawnLocationInit: (World) -> Location = { Location(it, 0.0, 0.0, 0.0) }
): World {
    val mock = mock(World::class.java)
    `when`(mock.name).thenReturn(name)
    `when`(mock.spawnLocation).thenReturn(spawnLocationInit(mock))
    `when`(mock.getChunkAt(any(Location::class.java)))
        .thenAnswer {
            val location = it.getArgument<Any>(0) as Location
            mockChunk(location.blockX / 16, location.blockZ / 16)
        }
    return mock
}

fun mockChunk(x: Int, z: Int): Chunk {
    val mock = mock(Chunk::class.java)
    `when`(mock.x).thenReturn(x)
    `when`(mock.z).thenReturn(z)
    return mock
}