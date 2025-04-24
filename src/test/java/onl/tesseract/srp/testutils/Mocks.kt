package onl.tesseract.srp.testutils

import org.bukkit.Location
import org.bukkit.World
import org.mockito.Mockito
import org.mockito.Mockito.`when`

fun mockWorld(
    name: String = "mockWorld",
    spawnLocationInit: (World) -> Location = { Location(it, 0.0, 0.0, 0.0) }
): World {
    val mock = Mockito.mock(World::class.java)
    `when`(mock.name).thenReturn(name)
    `when`(mock.spawnLocation).thenReturn(spawnLocationInit(mock))
    return mock
}