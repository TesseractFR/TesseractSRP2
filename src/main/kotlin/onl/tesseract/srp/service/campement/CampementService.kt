package onl.tesseract.srp.service.campement

import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import onl.tesseract.lib.logger.LoggerFactory
import onl.tesseract.lib.service.ServiceContainer
import onl.tesseract.srp.controller.event.campement.ChunkNotificationListener
import onl.tesseract.srp.domain.campement.Campement
import onl.tesseract.srp.repository.hibernate.CampementRepository
import org.bukkit.Bukkit
import org.bukkit.Location
import org.slf4j.Logger
import org.springframework.stereotype.Service
import org.springframework.context.annotation.Lazy
import java.util.*

private val logger: Logger = LoggerFactory.getLogger(CampementService::class.java)

@Service
open class CampementService(
    private val repository: CampementRepository,
    @Lazy private val chunkNotificationListener: ChunkNotificationListener,
    private val campementBorderRenderer: CampementBorderRenderer
) {
    @PostConstruct
    fun registerInServiceContainer() {
        ServiceContainer.getInstance().registerService(CampementService::class.java, this)
    }

    open fun getCampementByOwner(ownerID: UUID): Campement? {
        return repository.getById(ownerID)
    }

    open fun getCampementByChunk(chunk: String): Campement? {
        return repository.getCampementByChunk(chunk)
    }

    @Transactional
    open fun createCampement(ownerID: UUID, listChunks: List<String>, spawnLocation: Location): Boolean {
        for (chunk in listChunks) {
            if (repository.isChunkClaimed(chunk)) {
                return false
            }
        }
        val campement = Campement(
            id = UUID.randomUUID(),
            ownerID = ownerID,
            trustedPlayers = emptyList(),
            nbChunks = listChunks.size,
            listChunks = listChunks,
            campLevel = 1,
            spawnLocation = spawnLocation,
        )
        repository.save(campement)
        updatePlayerCampementLocation(ownerID)
        return true
    }

    @Transactional
    open fun deleteCampement(id: UUID) {
        repository.deleteById(id)
    }

    @Transactional
    open fun setSpawnpoint(ownerID: UUID, newLocation: Location) {
        val campement = repository.getById(ownerID) ?: return
        repository.save(campement.setSpawnpoint(newLocation))
    }

    /**
     * Increments the level of the player's camp.
     * @param ownerID The UUID of the player who owns the camp.
     * @return The new camp level if successful, or null if the camp does not exist.
     */
    @Transactional
    open fun incrementCampLevel(ownerID: UUID): Int? {
        val campement = repository.getById(ownerID) ?: return null
        campement.campLevel += 1
        repository.save(campement)
        return campement.campLevel
    }


    enum class AnnexationResult {
        SUCCESS, ALREADY_OWNED, ALREADY_CLAIMED, NOT_ADJACENT
    }

    /**
     * Attempts to claim a chunk for the player's camp.
     * @param ownerID The UUID of the player claiming the chunk.
     * @param chunk The chunk coordinates in the format "x,z".
     * @return The result of the annexation attempt.
     */
    open fun claimChunk(ownerID: UUID, chunk: String): AnnexationResult {
        val campement = repository.getById(ownerID) ?: return AnnexationResult.NOT_ADJACENT
        if (campement.listChunks.contains(chunk)) {
            return AnnexationResult.ALREADY_OWNED
        }
        if (repository.isChunkClaimed(chunk)) {
            return AnnexationResult.ALREADY_CLAIMED
        }
        val (chunkX, chunkZ) = chunk.split(",").map { it.toInt() }
        val isAdjacent = campement.listChunks.any { existingChunk ->
            val (existingX, existingZ) = existingChunk.split(",").map { it.toInt() }
            (existingX == chunkX && (existingZ == chunkZ - 1 || existingZ == chunkZ + 1)) || // Même X, Z ±1
                    (existingZ == chunkZ && (existingX == chunkX - 1 || existingX == chunkX + 1))    // Même Z, X ±1
        }
        if (!isAdjacent) {
            return AnnexationResult.NOT_ADJACENT
        }
        campement.listChunks += chunk
        campement.nbChunks = campement.listChunks.size
        repository.save(campement)
        updatePlayerCampementLocation(ownerID)
        updateBordersDisplayAfterClaim(ownerID)
        return AnnexationResult.SUCCESS
    }

    /**
     * Attempts to unclaim a chunk from the player's camp.
     * @param ownerID The UUID of the player unclaiming the chunk.
     * @param chunk The chunk coordinates in the format "x,z".
     * @return True if the chunk was successfully unclaimed, false otherwise.
     */
    open fun unclaimChunk(ownerID: UUID, chunk: String): Boolean {
        val campement = repository.getById(ownerID) ?: return false
        if (campement.listChunks.size == 1) {
            return false
        }
        if (!campement.listChunks.contains(chunk)) {
            return false
        }
        val updatedChunks = campement.listChunks.toMutableList()
        updatedChunks.remove(chunk)
        if (updatedChunks.isEmpty()) {
            repository.deleteById(campement.id)
            return true
        }
        campement.listChunks = updatedChunks
        campement.nbChunks = updatedChunks.size
        repository.save(campement)
        updatePlayerCampementLocation(ownerID)
        updateBordersDisplayAfterClaim(ownerID)
        return true
    }

    /**
     * Handles the process of claiming or unclaiming a chunk and returns a message indicating the result.
     * @param ownerID The UUID of the player performing the action.
     * @param chunk The chunk coordinates in the format "x,z".
     * @param claim True to claim the chunk, false to unclaim it.
     * @return A formatted string message describing the outcome of the operation.
     */
    open fun handleClaimUnclaim(ownerID: UUID, chunk: String, claim: Boolean): String {
        val campement = getCampementByOwner(ownerID)
            ?: return "§cTu ne possèdes pas de campement. Utilise §e/campement create §cpour en créer un !"

        return if (claim) {
            when (claimChunk(ownerID, chunk)) {
                AnnexationResult.SUCCESS -> "§aLe chunk ($chunk) a été annexé avec succès !"
                AnnexationResult.ALREADY_OWNED -> "§eTu possèdes déjà ce chunk."
                AnnexationResult.ALREADY_CLAIMED -> "§cCe chunk appartient à un autre campement."
                AnnexationResult.NOT_ADJACENT -> "§cTu dois sélectionner un chunk collé à ton campement."
            }
        } else {
            if (!campement.listChunks.contains(chunk)) {
                return "§cCe chunk ne fait pas partie de ton campement."
            }
            if (campement.listChunks.size == 1) {
                return "§cTu ne peux pas supprimer ton dernier chunk ! Si tu veux supprimer ton campement, utilise §e/campement delete"
            }
            unclaimChunk(ownerID, chunk)
            return "§aLe chunk ($chunk) a été retiré de ton campement !"
        }
    }

    /**
     * Updates the player's current campement location in the cache.
     * This ensures the correct campement status is maintained after a claim/unclaim.
     * @param playerId The UUID of the player.
     */
    private fun updatePlayerCampementLocation(playerId: UUID) {
        val player = Bukkit.getPlayer(playerId) ?: return
        val chunkX = player.location.chunk.x
        val chunkZ = player.location.chunk.z
        chunkNotificationListener.updatePlayerCampementCache(playerId, chunkX, chunkZ, notify = false)
    }

    /**
     * Updates the player's camp borders after a claim or unclaim action.
     * @param playerID UUID of the player.
     */
    private fun updateBordersDisplayAfterClaim(playerID: UUID) {
        val player = Bukkit.getPlayer(playerID) ?: return
        val campement = getCampementByOwner(playerID) ?: return
        val chunks = campement.listChunks.map { it.split(",").map(String::toInt) }

        // Réaffiche les nouvelles bordures
        campementBorderRenderer.showBorders(player, chunks)
    }


    /**
     * Retrieves whether a player can interact within a specific chunk.
     * This checks if the chunk belongs to the player or if they are trusted in the owning camp.
     * @param playerID The UUID of the player attempting interaction.
     * @param chunk The chunk coordinates in the format "x,z".
     * @return True if the player can interact, false otherwise.
     */
    open fun canInteractInChunk(playerID: UUID, chunk: String): Boolean {
        val campement = repository.getCampementByChunk(chunk) ?: return true
        return campement.ownerID == playerID || campement.trustedPlayers.contains(playerID)
    }

    /**
     * Adds a trusted player to the owner's camp, allowing them to interact with camp resources.
     * @param ownerID The UUID of the camp owner.
     * @param trustedPlayerID The UUID of the player being added to the trusted list.
     * @return True if the player was successfully added, false if they were already trusted.
     */
    @Transactional
    open fun trustPlayer(ownerID: UUID, trustedPlayerID: UUID): Boolean {
        val campement = repository.getById(ownerID) ?: return false
        if (trustedPlayerID in campement.trustedPlayers) {
            return false
        }
        campement.trustedPlayers += trustedPlayerID
        repository.save(campement)
        return true
    }

    /**
     * Removes a trusted player from the owner's camp, revoking their interaction privileges.
     * @param ownerID The UUID of the camp owner.
     * @param trustedPlayerID The UUID of the player being removed from the trusted list.
     * @return True if the player was successfully removed, false if they were not previously trusted.
     */
    @Transactional
    open fun untrustPlayer(ownerID: UUID, trustedPlayerID: UUID): Boolean {
        val campement = repository.getById(ownerID) ?: return false
        if (trustedPlayerID !in campement.trustedPlayers) {
            return false
        }
        campement.trustedPlayers -= trustedPlayerID
        repository.save(campement)
        return true
    }

}
