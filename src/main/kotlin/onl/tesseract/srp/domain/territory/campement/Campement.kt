package onl.tesseract.srp.domain.territory.campement

import onl.tesseract.srp.domain.territory.ChunkCoord
import onl.tesseract.srp.domain.territory.Territory
import onl.tesseract.srp.domain.territory.TerritoryChunk
import org.bukkit.Location
import java.util.*

class Campement(
    val ownerID: UUID,
    var campLevel: Int,
    spawnLocation: Location,
    trustedPlayers: Set<UUID> = mutableSetOf(),
) : Territory<CampementChunk>(spawnLocation)

{
    private val _trustedPlayers: MutableSet<UUID> = trustedPlayers.toMutableSet()
    val trustedPlayers: Set<UUID>
        get() = _trustedPlayers

    fun addTrustedPlayer(playerID: UUID): Boolean {
        return _trustedPlayers.add(playerID)
    }

    fun removeTrustedPlayer(playerID: UUID): Boolean {
        return _trustedPlayers.remove(playerID)
    }

    override fun initChunk(location: Location): CampementChunk {
        return CampementChunk(ChunkCoord(location),this)
    }

    override fun canClaim(player: UUID): Boolean {
        return player==ownerID
    }

    override fun createClaimEvent(
        player: UUID
    ): CampementChunkClaimEvent {
        return CampementChunkClaimEvent(player)
    }

    override fun createUnclaimEvent(
        player: UUID
    ): CampementChunkUnclaimEvent {
        return CampementChunkUnclaimEvent(player)
    }

    fun claimInitialChunks() {
        _chunks.add(CampementChunk(ChunkCoord(spawnLocation),this))
    }

    override fun canSetSpawn(player: UUID): Boolean {
        return player == ownerID
    }
}

class CampementChunk(chunkCoord: ChunkCoord,val campement: Campement) : TerritoryChunk(chunkCoord){
    override fun getOwner(): Campement {
        return campement
    }

}
