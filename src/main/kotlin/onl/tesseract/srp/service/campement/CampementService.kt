package onl.tesseract.srp.service.campement

import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.event.EventService
import onl.tesseract.lib.logger.LoggerFactory
import onl.tesseract.lib.service.ServiceContainer
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.controller.event.campement.CampementChunkClaimEvent
import onl.tesseract.srp.controller.event.campement.CampementChunkUnclaimEvent
import onl.tesseract.srp.domain.campement.Campement
import onl.tesseract.srp.domain.campement.CampementChunk
import onl.tesseract.srp.domain.world.SrpWorld
import onl.tesseract.srp.repository.hibernate.CampementRepository
import onl.tesseract.srp.service.player.SrpPlayerService
import onl.tesseract.srp.service.world.WorldService
import onl.tesseract.srp.util.*
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.entity.Player
import org.slf4j.Logger
import org.springframework.stereotype.Service
import java.util.*

private val logger: Logger = LoggerFactory.getLogger(CampementService::class.java)
private const val CAMP_BORDER_COMMAND = "/campement border"

@Service
open class CampementService(
    private val repository: CampementRepository,
    private val eventService: EventService,
    private val worldService: WorldService,
    private val srpPlayerService: SrpPlayerService
) {
    @PostConstruct
    fun registerInServiceContainer() {
        ServiceContainer.getInstance().registerService(CampementService::class.java, this)
    }

    open fun getCampementByOwner(ownerID: UUID): Campement? {
        return repository.getById(ownerID)
    }

    open fun getCampementByChunk(x: Int, z: Int): Campement? {
        return repository.getCampementByChunk(x, z)
    }

    open fun getAllCampements(): List<Campement> = repository.findAll()

    /**
     * Check if the player has a campement and send him an error message if not.
     */
    open fun hasCampement(sender: Player): Boolean {
        val has = getCampementByOwner(sender.uniqueId) != null
        if (!has) {
            sender.sendMessage(CampementChatError + "Tu ne possèdes pas de campement. Utilise "
                    + Component.text("/campement create", NamedTextColor.GOLD)
                    + Component.text(" pour en créer un !"))
        }
        return has
    }

    @Transactional
    open fun createCampement(ownerID: UUID, spawnLocation: Location): Boolean {
        val chunkX = spawnLocation.chunk.x
        val chunkZ = spawnLocation.chunk.z
        val chunk = CampementChunk(chunkX, chunkZ)
        if (worldService.getSrpWorld(spawnLocation) != SrpWorld.Elysea)
            return false

        if (repository.isChunkClaimed(chunkX, chunkZ)) {
            return false
        }
        val player = srpPlayerService.getPlayer(ownerID)
        val campLevel = player.rank.campLevel

        val campement = Campement(
            ownerID = ownerID,
            trustedPlayers = emptySet(),
            chunks = mutableSetOf(chunk),
            campLevel = campLevel,
            spawnLocation = spawnLocation,
        )

        logger.info("New campement (level $campLevel) created for owner $ownerID")
        repository.save(campement)
        eventService.callEvent(CampementChunkClaimEvent(ownerID, chunk))
        return true
    }

    @Transactional
    open fun deleteCampement(id: UUID) {
        logger.info("Deleting campement $id")
        repository.deleteById(id)
    }

    @Transactional
    open fun setSpawnpoint(ownerID: UUID, newLocation: Location): Boolean {
        val campement = repository.getById(ownerID)
            ?: throw IllegalArgumentException("Campement $ownerID does not exist")

        if (worldService.getSrpWorld(newLocation) != SrpWorld.Elysea)
            return false
        val result = campement.setSpawnpoint(newLocation)
        if (result)
            repository.save(campement)
        return result
    }

    /**
     * Increments the level of the player's camp.
     * @param ownerID The UUID of the player who owns the camp.
     * @return The new camp level if successful, or null if the camp does not exist.
     */
    @Transactional
    open fun setCampLevel(ownerID: UUID, level: Int): Boolean {
        val campement = repository.getById(ownerID)
            ?: throw IllegalArgumentException("Campement from $ownerID does not exist")

        if (campement.campLevel != level) {
            campement.campLevel = level
            logger.info("Campement level from $ownerID set to $level")
            repository.save(campement)
            return true
        }
        return false
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
    open fun claimChunk(ownerID: UUID, x: Int, z: Int): AnnexationResult {
        val campement = repository.getById(ownerID)
            ?: throw IllegalArgumentException("Campement $ownerID does not exist")

        val chunk = CampementChunk(x, z)
        if (chunk in campement.chunks) {
            return AnnexationResult.ALREADY_OWNED
        }

        if (repository.isChunkClaimed(x, z)) {
            return AnnexationResult.ALREADY_CLAIMED
        }

        val isAdjacent = campement.chunks.isNotEmpty() &&
                TerritoryChunks.isAdjacentToAny(campement.chunks, chunk, { it.x }, { it.z })

        if (!isAdjacent) {
            return AnnexationResult.NOT_ADJACENT
        }

        campement.addChunk(chunk)
        logger.info("Campement $ownerID claimed chunk $chunk")
        repository.save(campement)
        eventService.callEvent(CampementChunkClaimEvent(ownerID, chunk))
        return AnnexationResult.SUCCESS
    }


    /**
     * Attempts to unclaim a chunk from the player's camp.
     * @param ownerID The UUID of the player unclaiming the chunk.
     * @param chunk The chunk coordinates in the format "x,z".
     * @return True if the chunk was successfully unclaimed, false otherwise.
     */
    open fun unclaimChunk(ownerID: UUID, x: Int, z: Int): Boolean {
        val campement = repository.getById(ownerID)
            ?: throw IllegalArgumentException("Campement $ownerID does not exist")

        val chunk = CampementChunk(x, z)
        val result = campement.unclaim(chunk)
        if (!result) return false

        logger.info("Campement $ownerID unclaimed chunk $chunk")
        repository.save(campement)
        eventService.callEvent(CampementChunkUnclaimEvent(ownerID, chunk))
        return true
    }

    /**
     * Handles the process of claiming or unclaiming a chunk and returns a message indicating the result.
     * @param ownerID The player performing the action.
     * @param chunk Chunk to claim/unclaim
     * @param claim True to claim the chunk, false to unclaim it.
     * @return A formatted string message describing the outcome of the operation.
     */
    open fun handleClaimUnclaim(owner: Player, chunk: Chunk, claim: Boolean){
        val campement = getCampementByOwner(owner.uniqueId)!!
        if (worldService.getSrpWorld(chunk.world) != SrpWorld.Elysea) {
            owner.sendMessage(CampementChatError + "Tu ne peux pas claim dans ce monde.")
            return
        }
        val campementChunk = CampementChunk(chunk.x, chunk.z)
        if (claim) {
            when (claimChunk(owner.uniqueId, chunk.x, chunk.z)) {
                AnnexationResult.SUCCESS -> owner.sendMessage(
                    CampementChatSuccess + "Le chunk (${chunk.x}, ${chunk.z}) a été annexé avec succès.")

                AnnexationResult.ALREADY_OWNED -> owner.sendMessage(
                    CampementChatFormat + "Tu possèdes déjà ce chunk. Visualise les bordures avec "
                            + Component.text(CAMP_BORDER_COMMAND, NamedTextColor.GOLD)
                            + ".")

                AnnexationResult.ALREADY_CLAIMED -> owner.sendMessage(
                    CampementChatError + "Ce chunk appartient à un autre campement. Visualise les bordures de ton campement avec "
                            + Component.text(CAMP_BORDER_COMMAND, NamedTextColor.GOLD)
                            + ".")

                AnnexationResult.NOT_ADJACENT -> owner.sendMessage(
                    CampementChatError + "Tu dois sélectionner un chunk collé à ton campement. Visualise les bordures avec "
                            + Component.text(CAMP_BORDER_COMMAND, NamedTextColor.GOLD)
                            + ".")
            }
        } else {
            if (!campement.chunks.contains(campementChunk)) {
                owner.sendMessage(
                    CampementChatError + "Ce chunk ne fait pas partie de ton campement. Visualise les bordures avec "
                            + Component.text(CAMP_BORDER_COMMAND, NamedTextColor.GOLD)
                            + ".")
                return
            }
            if (campement.chunks.size == 1) {
                owner.sendMessage(
                    CampementChatError + "Tu ne peux pas supprimer ton dernier chunk ! Si tu veux supprimer ton campement, utilise "
                            + Component.text("/campement delete", NamedTextColor.GOLD)
                            + ".")
                return
            }
            if (campementChunk == CampementChunk(campement.spawnLocation)) {
                owner.sendMessage(
                    CampementChatError + "Tu ne peux pas désannexer ce chunk, il contient le point de spawn de ton campement. Déplace-le dans un autre chunk avec "
                            + Component.text("/campement setspawn", NamedTextColor.GOLD)
                            + " avant de retirer celui-ci.")
                return
            }
            if (!TerritoryChunks.isUnclaimValid(campement.chunks, campementChunk, { it.x }, { it.z })) {
                owner.sendMessage(
                    CampementChatError + "Tu ne peux pas désannexer ce chunk, cela diviserait ton campement en plusieurs parties. Visualise les bordures avec "
                            + Component.text(CAMP_BORDER_COMMAND, NamedTextColor.GOLD)
                            + ".")
                return
            }

            unclaimChunk(owner.uniqueId, chunk.x, chunk.z)
            owner.sendMessage(
                CampementChatSuccess + "Le chunk (${chunk.x}, ${chunk.z}) a été retiré de ton campement.")
        }
    }

    /**
     * Retrieves whether a player can interact within a specific chunk.
     * This checks if the chunk belongs to the player or if they are trusted in the owning camp.
     * @param playerID The UUID of the player attempting interaction.
     * @param chunk The chunk coordinates in the format "x,z".
     * @return True if the player can interact, false otherwise.
     */
    open fun canInteractInChunk(playerID: UUID, chunk: Chunk): InteractionAllowResult {
        if (worldService.getSrpWorld(chunk.world) != SrpWorld.Elysea) return InteractionAllowResult.Ignore

        val campement = repository.getCampementByChunk(chunk.x, chunk.z) ?: return InteractionAllowResult.Deny
        return if (campement.ownerID == playerID || campement.trustedPlayers.contains(playerID))
            InteractionAllowResult.Allow
        else
            InteractionAllowResult.Deny
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
        if (!campement.addTrustedPlayer(trustedPlayerID)) return false
        logger.info("Player $trustedPlayerID is now a trusted member of campement $ownerID")
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
        if (!campement.removeTrustedPlayer(trustedPlayerID)) return false
        logger.info("Removed player $trustedPlayerID from trusted members of of campement $ownerID")
        repository.save(campement)
        return true
    }

}
