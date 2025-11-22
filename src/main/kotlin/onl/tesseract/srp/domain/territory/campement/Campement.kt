package onl.tesseract.srp.domain.territory.campement

import onl.tesseract.srp.domain.territory.ChunkCoord
import onl.tesseract.srp.domain.territory.Coordinate
import onl.tesseract.srp.domain.territory.Territory
import java.util.*

class Campement(
    val ownerID: UUID,
    var campLevel: Int,
    spawnLocation: Coordinate,
    trustedPlayers: MutableSet<UUID> = mutableSetOf(),
) : Territory<CampementChunk>(spawnLocation,trustedPlayers)

{
    override fun initChunk(chunkCoord: ChunkCoord): CampementChunk {
        return CampementChunk(chunkCoord,this)
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

    override fun claimInitialChunks() {
        _chunks.add(CampementChunk(spawnContainer.getSpawnpoint().chunkCoord,this))
    }

    override fun canSetSpawn(player: UUID): Boolean {
        return player == ownerID
    }

    override fun canBuild(player: UUID): Boolean {
        TODO("Not yet implemented")
    }

    override fun canOpenChest(player: UUID): Boolean {
        TODO("Not yet implemented")
    }
}


