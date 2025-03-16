package onl.tesseract.srp.domain.campement

import java.util.*

class Campement(
    val id: UUID,
    val ownerID: UUID,
    val trustedPlayers: List<UUID>,
    val chunks: Int,
    val listChunks: List<String>,
    val campLevel: Int
) {

    fun upgradeCampLevel(): Campement {
        return Campement(id, ownerID, trustedPlayers, chunks, listChunks, campLevel + 1)
    }

    fun addTrustedPlayer(playerID: UUID): Campement {
        if (!trustedPlayers.contains(playerID)) {
            return Campement(id, ownerID, trustedPlayers + playerID, chunks, listChunks, campLevel)
        }
        return this
    }
}
