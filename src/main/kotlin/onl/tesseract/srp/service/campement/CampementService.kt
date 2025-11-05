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
private const val CAMP_PROTECTION_RADIUS = 2
private const val SPAWN_PROTECTION_RADIUS = 15
const val CAMP_BORDER_COMMAND = "/campement border"

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
    open fun createCampement(ownerID: UUID, spawnLocation: Location): CampementCreationResult {
        val player = srpPlayerService.getPlayer(ownerID)

        val errors = TerritoryClaimManager.performCreationChecks(
            location = spawnLocation,
            player = player,
            policy = TerritoryClaimManager.CreationPolicy(
                isCorrectWorld = { worldService.getSrpWorld(it) == SrpWorld.Elysea },
                spawnProtectionRadius = SPAWN_PROTECTION_RADIUS,
                protectionRadius = CAMP_PROTECTION_RADIUS,
                minMoney = null,
                minRank = null
            ),
            alreadyHasTerritory = { repository.getById(ownerID) != null },
            isNameTaken = null,
            isChunkTaken = { cx, cz ->
                val other = repository.getCampementByChunk(cx, cz)
                other != null && other.ownerID != ownerID
            }
        )

        if (errors.isNotEmpty()) {
            val mapped = errors.map {
                when (it) {
                    CreationError.ALREADY_HAS_TERRITORY       -> CampementCreationResult.Reason.AlreadyHasCampement
                    CreationError.INVALID_WORLD               -> CampementCreationResult.Reason.InvalidWorld
                    CreationError.NEAR_SPAWN                  -> CampementCreationResult.Reason.NearSpawn
                    CreationError.TOO_CLOSE_TO_OTHER_TERRITORY-> CampementCreationResult.Reason.NearCampement
                    CreationError.ON_OTHER_TERRITORY -> CampementCreationResult.Reason.OnOtherCampement
                    CreationError.NAME_TAKEN -> CampementCreationResult.Reason.Ignored
                    CreationError.NOT_ENOUGH_MONEY -> CampementCreationResult.Reason.Ignored
                    CreationError.RANK_TOO_LOW -> CampementCreationResult.Reason.Ignored
                }
            }
            return CampementCreationResult.failed(mapped)
        }

        val chunk = CampementChunk(spawnLocation.chunk.x, spawnLocation.chunk.z)
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

        return CampementCreationResult.success(campement)
    }


    @Transactional
    open fun deleteCampement(id: UUID) {
        logger.info("Deleting campement $id")
        repository.deleteById(id)
    }

    @Transactional
    open fun setSpawnpoint(ownerID: UUID, newLocation: Location): CampementSetSpawnResult {
        val campement = repository.getById(ownerID)
            ?: throw IllegalArgumentException("Campement $ownerID does not exist")

        val res = TerritorySpawnManager.setSpawn(
            newLocation,
            policy = TerritorySpawnManager.SetSpawnPolicy(
                isCorrectWorld = { worldService.getSrpWorld(it) == SrpWorld.Elysea },
                requireInsideTerritory = true
            ),
            io = TerritorySpawnManager.SetSpawnOperations(
                authorized = { true },
                isInsideTerritory = { loc ->
                    campement.chunks.contains(CampementChunk(loc.chunk.x, loc.chunk.z))
                },
                setAndPersist = { loc ->
                    val ok = campement.setSpawnpoint(loc)
                    if (ok) repository.save(campement)
                    ok
                }
            )
        )

        return when (res) {
            TerritorySpawnManager.SetSpawnResult.SUCCESS           -> CampementSetSpawnResult.SUCCESS
            TerritorySpawnManager.SetSpawnResult.INVALID_WORLD     -> CampementSetSpawnResult.INVALID_WORLD
            TerritorySpawnManager.SetSpawnResult.OUTSIDE_TERRITORY -> CampementSetSpawnResult.OUTSIDE_TERRITORY
            TerritorySpawnManager.SetSpawnResult.NOT_AUTHORIZED    -> CampementSetSpawnResult.NOT_AUTHORIZED
        }
    }

    open fun getCampSpawn(ownerID: UUID): Location? =
        repository.getById(ownerID)?.spawnLocation

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
        SUCCESS, ALREADY_OWNED, ALREADY_CLAIMED, NOT_ADJACENT, TOO_CLOSE, NOT_ALLOWED
    }

    open fun claimChunk(ownerID: UUID, x: Int, z: Int): AnnexationResult {
        val camp = repository.getById(ownerID)
            ?: throw IllegalArgumentException("Campement $ownerID does not exist")
        val target = CampementChunk(x, z)

        val claimRes = TerritoryClaimManager.claim(
            owned = camp.chunks,
            target = target,
            policy = ClaimPolicy(
                requireAdjacent = true,
                allowFirstAnywhere = false,
                protectionRadius = CAMP_PROTECTION_RADIUS
            ),
            io = ClaimOperations(
                authorized = { true },
                takenElsewhere = { c -> repository.isChunkClaimed(c.x, c.z) },
                addAndPersist = { c ->
                    camp.addChunk(c)
                    repository.save(camp)
                    eventService.callEvent(CampementChunkClaimEvent(ownerID, c))
                },
                coords = { c -> c.x to c.z },
                isTakenAt = { cx, cz ->
                    val other = repository.getCampementByChunk(cx, cz)
                    other != null && other.ownerID != ownerID
                }
            )
        )
        return when (claimRes) {
            ClaimResult.SUCCESS        -> AnnexationResult.SUCCESS
            ClaimResult.ALREADY_OWNED  -> AnnexationResult.ALREADY_OWNED
            ClaimResult.ALREADY_TAKEN  -> AnnexationResult.ALREADY_CLAIMED
            ClaimResult.NOT_ADJACENT   -> AnnexationResult.NOT_ADJACENT
            ClaimResult.NOT_ALLOWED    -> AnnexationResult.NOT_ALLOWED
            ClaimResult.TOO_CLOSE      -> AnnexationResult.TOO_CLOSE
        }
    }

    open fun unclaimChunk(ownerID: UUID, x: Int, z: Int): Boolean {
        val camp = repository.getById(ownerID)
            ?: throw IllegalArgumentException("Campement $ownerID does not exist")
        val target = CampementChunk(x, z)
        val unclaimRes = TerritoryClaimManager.unclaim(
            owned = camp.chunks,
            target = target,
            policy = ClaimPolicy(
                forbidLastRemoval = true,
                forbidSpawnRemoval = true,
                keepConnected = true
            ),
            io = UnclaimOperations(
                authorized = { true },
                isSpawnChunk = { c -> c == CampementChunk(camp.spawnLocation) },
                removeAndPersist = { c ->
                    camp.unclaim(c)
                    repository.save(camp)
                    eventService.callEvent(CampementChunkUnclaimEvent(ownerID, c))
                },
                coords = { c -> c.x to c.z }
            )
        )
        return unclaimRes == UnclaimResult.SUCCESS
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

enum class CampementSetSpawnResult { SUCCESS, INVALID_WORLD, OUTSIDE_TERRITORY, NOT_AUTHORIZED }

data class CampementCreationResult(val campement: Campement?, val reason: List<Reason>) {

    enum class Reason {
        InvalidWorld,
        NearSpawn,
        NearCampement,
        OnOtherCampement,
        AlreadyHasCampement,
        Ignored
    }

    companion object {
        fun failed(reasons: List<Reason>) = CampementCreationResult(null, reasons)
        fun success(campement: Campement) = CampementCreationResult(campement, emptyList())
    }
}
