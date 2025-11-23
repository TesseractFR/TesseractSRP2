package onl.tesseract.srp.domain.territory.container

import onl.tesseract.srp.domain.commun.ChunkCoord
import onl.tesseract.srp.domain.commun.Coordinate
import onl.tesseract.srp.domain.territory.enum.SetSpawnResult
import java.util.UUID

interface SpawnContainer {
    fun setSpawnpoint(coordinate: Coordinate, player: UUID): SetSpawnResult
    fun isSpawnChunk(chunkCoord: ChunkCoord): Boolean
    fun canSetSpawn(player: UUID): Boolean
    fun getSpawnpoint() : Coordinate
}

open class DefaultSpawnContainer(private var spawnpoint: Coordinate) : SpawnContainer{
    override fun setSpawnpoint(
        coordinate: Coordinate,
        player: UUID,
    ): SetSpawnResult {
        if (!canSetSpawn(player)) return SetSpawnResult.NOT_AUTHORIZED
        this.spawnpoint = coordinate
        return SetSpawnResult.SUCCESS
    }

    override fun isSpawnChunk(chunkCoord: ChunkCoord): Boolean {
        return spawnpoint.chunkCoord == chunkCoord
    }

    override fun canSetSpawn(player: UUID): Boolean {
        throw IllegalAccessError("La méthode doit être override")
    }

    override fun getSpawnpoint(): Coordinate {
        return spawnpoint
    }

}