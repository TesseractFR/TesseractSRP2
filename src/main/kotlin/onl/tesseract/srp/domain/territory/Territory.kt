package onl.tesseract.srp.domain.territory

import onl.tesseract.srp.domain.commun.ChunkCoord
import onl.tesseract.srp.domain.commun.Coordinate
import onl.tesseract.srp.domain.territory.container.ClaimContainer
import onl.tesseract.srp.domain.territory.container.DefaultSpawnContainer
import onl.tesseract.srp.domain.territory.container.DefaultTrustContainer
import onl.tesseract.srp.domain.territory.container.SpawnContainer
import onl.tesseract.srp.domain.territory.container.TrustContainer
import onl.tesseract.srp.domain.territory.enum.SetSpawnResult
import onl.tesseract.srp.domain.territory.enum.UnclaimResult
import java.util.*

abstract class Territory<TC : TerritoryChunk>(
    val id: UUID,
    spawnLocation: Coordinate,
    trustedPlayers: MutableSet<UUID> = mutableSetOf(),
    val spawnContainer: SpawnContainer = DefaultSpawnContainer(spawnLocation),
    val trustContainer: TrustContainer = DefaultTrustContainer(trustedPlayers)
)  : ClaimContainer<TC>() , SpawnContainer by spawnContainer, TrustContainer by trustContainer{
    /**
     * Sets the spawn point of the guild.
     * The new location must be within one of the guild's chunks.
     * @return true if the spawn point was set, false otherwise
     */
    override fun setSpawnpoint(coordinate: Coordinate, player: UUID): SetSpawnResult {
        if (!canSetSpawn(player)) return SetSpawnResult.NOT_AUTHORIZED
        if (!hasChunk(coordinate.chunkCoord)) return SetSpawnResult.OUTSIDE_TERRITORY
        return spawnContainer.setSpawnpoint(coordinate,player)
    }

    override fun unclaimChunk(chunkCoord: ChunkCoord, player : UUID): UnclaimResult {
        if(isSpawnChunk(chunkCoord)) return UnclaimResult.IS_SPAWN_CHUNK
        return super.unclaimChunk(chunkCoord, player)
    }

    abstract fun canBuild(player: UUID) : Boolean

    abstract fun canOpenChest(player: UUID) : Boolean

    abstract fun claimInitialChunks()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as Territory<*>
        return id == other.id
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + this::class.hashCode()
        return result
    }
}