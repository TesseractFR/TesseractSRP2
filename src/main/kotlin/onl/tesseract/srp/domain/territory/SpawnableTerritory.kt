package onl.tesseract.srp.domain.territory

import onl.tesseract.srp.domain.commun.enum.SetSpawnResult
import org.bukkit.Location
import java.util.UUID

interface SpawnableTerritory {
    fun setSpawnpoint(location: Location,player: UUID): SetSpawnResult
    fun isSpawnChunk(location: Location): Boolean
    fun canSetSpawn(player: UUID): Boolean
}