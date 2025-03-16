package onl.tesseract.srp.domain.campement

import org.bukkit.Location
import java.util.*

class Campement(
    val ownerID: UUID,
    trustedPlayers: Set<UUID>,
    chunks: Set<CampementChunk>,
    var campLevel: Int,
    var spawnLocation: Location
) {
    private val _trustedPlayers: MutableSet<UUID> = trustedPlayers.toMutableSet()
    val trustedPlayers: Set<UUID>
        get() = _trustedPlayers

    private val _chunks: MutableSet<CampementChunk> = chunks.toMutableSet()
    val chunks: Set<CampementChunk>
        get() = _chunks

    fun setSpawnpoint(newLocation: Location): Boolean {
        if (!isLocationInChunks(newLocation)) {
            return false
        }
        this.spawnLocation = newLocation
        return true
    }

    fun isLocationInChunks(location: Location): Boolean {
        return chunks.contains(CampementChunk(location))
    }

    fun addChunk(chunk: CampementChunk) {
        if (!_chunks.add(chunk)) {
            throw IllegalArgumentException("Le chunk (${chunk.x}, ${chunk.z}) est déjà présent dans ton campement.")
        }
    }

    fun unclaim(chunk: CampementChunk): Boolean {
        if (_chunks.size == 1) {
            return false
        }
        return _chunks.remove(chunk)
    }

    fun addTrustedPlayer(playerID: UUID): Boolean {
        return _trustedPlayers.add(playerID)
    }

    fun removeTrustedPlayer(playerID: UUID): Boolean {
        return _trustedPlayers.remove(playerID)
    }
}

data class CampementChunk(val x: Int, val z: Int) {

    constructor(location: Location): this(location.chunk.x, location.chunk.z)

    override fun toString(): String = "($x, $z)"
}
