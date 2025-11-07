package onl.tesseract.srp.domain.territory

import org.bukkit.Location

interface SpawnableTerritory {
    fun setSpawnpoint(location: Location): Boolean
    fun isSpawnChunk(location: Location): Boolean
}