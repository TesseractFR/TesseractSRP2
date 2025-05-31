package onl.tesseract.srp.domain.campement

import onl.tesseract.srp.domain.Claim
import org.bukkit.Location
import java.util.*

class Campement(
    val ownerID: UUID,
    trustedPlayers: Set<UUID>,
    chunks: Set<Claim>,
    var campLevel: Int,
    var spawnLocation: Location
) {
    private val _trustedPlayers: MutableSet<UUID> = trustedPlayers.toMutableSet()
    val trustedPlayers: Set<UUID>
        get() = _trustedPlayers

    private val _chunks: MutableSet<Claim> = chunks.toMutableSet()
    val chunks: Set<Claim>
        get() = _chunks

    fun setSpawnpoint(newLocation: Location): Boolean {
        if (!isLocationInChunks(newLocation)) {
            return false
        }
        this.spawnLocation = newLocation
        return true
    }

    fun isLocationInChunks(location: Location): Boolean {
        return chunks.contains(Claim(location))
    }

    fun addChunk(chunk: Claim) {
        if (!_chunks.add(chunk)) {
            throw IllegalArgumentException("Le chunk (${chunk.x}, ${chunk.z}) est déjà présent dans ton campement.")
        }
    }

    fun unclaim(chunk: Claim): Boolean {
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

