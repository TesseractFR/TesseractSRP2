package onl.tesseract.srp.domain.campement

import org.bukkit.Location
import java.util.*

class Campement(
    val id: UUID,
    val ownerID: UUID,
    val trustedPlayers: List<UUID>,
    val chunks: Int,
    val listChunks: List<String>,
    val campLevel: Int,
    val spawnLocation: Location
) {

    fun setSpawnpoint(newLocation: Location): Campement {
        return Campement(id, ownerID, trustedPlayers, chunks, listChunks, campLevel, newLocation)
    }
}
