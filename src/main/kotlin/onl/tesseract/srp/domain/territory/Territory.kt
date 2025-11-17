package onl.tesseract.srp.domain.territory

import onl.tesseract.srp.domain.commun.enum.ClaimResult
import onl.tesseract.srp.domain.commun.enum.SetSpawnResult
import onl.tesseract.srp.domain.commun.enum.UnclaimResult
import org.bukkit.Location
import java.util.UUID

abstract class Territory<TC : TerritoryChunk>(
    var spawnLocation: Location,
)  : ClaimContainer<TC>() ,SpawnableTerritory{
    /**
     * Sets the spawn point of the guild.
     * The new location must be within one of the guild's chunks.
     * @return true if the spawn point was set, false otherwise
     */
    override fun setSpawnpoint(location: Location,player: UUID): SetSpawnResult {
        if (!hasChunk(location)) return SetSpawnResult.OUTSIDE_TERRITORY
        if (!canSetSpawn(player)) return SetSpawnResult.NOT_AUTHORIZED
        spawnLocation = location
        return SetSpawnResult.SUCCESS
    }

    override fun isSpawnChunk(location: Location): Boolean {
        return location.chunk == spawnLocation.chunk
    }

    override fun unclaimChunk(location: Location,player : UUID): UnclaimResult {
        if(isSpawnChunk(location)) return UnclaimResult.IS_SPAWN_CHUNK
        return super.unclaimChunk(location, player)
    }


}