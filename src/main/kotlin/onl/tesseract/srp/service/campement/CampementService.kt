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
) : TerritoryService<CampementChunk, UUID>() {
    @PostConstruct
    fun registerInServiceContainer() {
        ServiceContainer.getInstance().registerService(CampementService::class.java, this)
    }

    override val spawnProtectionRadius: Int = SPAWN_PROTECTION_RADIUS
    override val territoryProtectionRadius: Int = CAMP_PROTECTION_RADIUS

    private fun camp(ownerID: UUID): Campement =
        repository.getById(ownerID) ?: error("Campement $ownerID does not exist")

    override fun isCorrectWorld(loc: Location): Boolean = worldService.getSrpWorld(loc.world) == SrpWorld.Elysea
    override fun hasTerritory(ownerId: UUID): Boolean = repository.getById(ownerId) != null
    override fun isChunkTaken(x: Int, z: Int): Boolean = repository.getCampementByChunk(x, z) != null
    override fun isTakenByOther(ownerId: UUID, x: Int, z: Int): Boolean {
        val other = repository.getCampementByChunk(x, z) ?: return false
        return other.ownerID != ownerId
    }
    override fun ownerOf(x: Int, z: Int): UUID? = repository.getCampementByChunk(x, z)?.ownerID
    override fun getOwnedChunks(ownerId: UUID): MutableSet<CampementChunk> =
        camp(ownerId).chunks.toMutableSet()

    override fun chunkOf(x: Int, z: Int): CampementChunk = CampementChunk(x, z)
    override fun chunkOf(loc: Location): CampementChunk = CampementChunk(loc.chunk.x, loc.chunk.z)
    override fun coords(c: CampementChunk): Pair<Int, Int> = c.x to c.z

    override fun isAuthorizedToClaim(ownerId: UUID, requesterId: UUID): Boolean =
        ownerId == requesterId

    override fun isAuthorizedToUnclaim(ownerId: UUID, requesterId: UUID): Boolean =
        ownerId == requesterId

    override fun isAuthorizedToSetSpawn(ownerId: UUID, requesterId: UUID): Boolean =
        ownerId == requesterId

    override fun interactionOutcomeWhenNoOwner(): InteractionAllowResult =
        InteractionAllowResult.Deny

    override fun isMemberOrTrusted(ownerId: UUID, playerId: UUID): Boolean {
        val camp = repository.getById(ownerId) ?: return false
        return camp.ownerID == playerId || camp.trustedPlayers.contains(playerId)
    }

    override fun persistAfterClaim(ownerId: UUID, claimed: CampementChunk) {
        val c = camp(ownerId)
        c.addChunk(claimed)
        repository.save(c)
        eventService.callEvent(CampementChunkClaimEvent(ownerId, claimed))
    }

    override fun persistAfterUnclaim(ownerId: UUID, unclaimed: CampementChunk) {
        val c = camp(ownerId)
        c.unclaim(unclaimed)
        repository.save(c)
        eventService.callEvent(CampementChunkUnclaimEvent(ownerId, unclaimed))
    }

    override fun persistSpawn(ownerId: UUID, loc: Location): Boolean {
        val c = camp(ownerId)
        val ok = c.setSpawnpoint(loc)
        if (ok) repository.save(c)
        return ok
    }

    override fun isSpawnChunk(ownerId: UUID, c: CampementChunk): Boolean =
        c == CampementChunk(camp(ownerId).spawnLocation)

    open fun getCampementByOwner(ownerID: UUID): Campement? {
        return repository.getById(ownerID)
    }

    open fun getCampementByChunk(x: Int, z: Int): Campement? {
        return repository.getCampementByChunk(x, z)
    }

    open fun getAllCampements(): List<Campement> = repository.findAll()

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
        val errors = performCreationChecks(
            ownerId = ownerID,
            location = spawnLocation,
            playerMoney = null,
            playerRank = player.rank,
            minMoney = null,
            minRank = null,
            alreadyHas = (repository.getById(ownerID) != null),
            isNameTaken = null
        )

        if (errors.isNotEmpty()) {
            val mapped = errors.map {
                when (it) {
                    CreationError.ALREADY_HAS_TERRITORY -> CampementCreationResult.Reason.AlreadyHasCampement
                    CreationError.INVALID_WORLD -> CampementCreationResult.Reason.InvalidWorld
                    CreationError.NEAR_SPAWN -> CampementCreationResult.Reason.NearSpawn
                    CreationError.TOO_CLOSE_TO_OTHER_TERRITORY -> CampementCreationResult.Reason.NearCampement
                    CreationError.ON_OTHER_TERRITORY -> CampementCreationResult.Reason.OnOtherCampement
                    CreationError.NAME_TAKEN,
                    CreationError.NOT_ENOUGH_MONEY,
                    CreationError.RANK_TOO_LOW -> CampementCreationResult.Reason.Ignored
                }
            }
            return CampementCreationResult.failed(mapped)
        }
        val spawnChunk = CampementChunk(spawnLocation.chunk.x, spawnLocation.chunk.z)
        val campLevel = player.rank.campLevel
        val campement = Campement(
            ownerID = ownerID,
            trustedPlayers = emptySet(),
            chunks = mutableSetOf(spawnChunk),
            campLevel = campLevel,
            spawnLocation = spawnLocation
        )
        logger.info("New campement (level $campLevel) created for owner $ownerID")
        repository.save(campement)
        eventService.callEvent(CampementChunkClaimEvent(ownerID, spawnChunk))

        return CampementCreationResult.success(campement)
    }

    @Transactional
    open fun deleteCampement(id: UUID) {
        logger.info("Deleting campement $id")
        repository.deleteById(id)
    }

    @Transactional
    open fun setSpawnpoint(ownerID: UUID, newLocation: Location): CampementSetSpawnResult =
        when (doSetSpawn(ownerID, ownerID, newLocation)) {
            SetSpawnResult.SUCCESS -> CampementSetSpawnResult.SUCCESS
            SetSpawnResult.INVALID_WORLD -> CampementSetSpawnResult.INVALID_WORLD
            SetSpawnResult.OUTSIDE_TERRITORY -> CampementSetSpawnResult.OUTSIDE_TERRITORY
            SetSpawnResult.NOT_AUTHORIZED -> CampementSetSpawnResult.NOT_AUTHORIZED
        }

    open fun getCampSpawn(ownerID: UUID): Location? = repository.getById(ownerID)?.spawnLocation

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

    enum class CampClaimResult {
        SUCCESS, ALREADY_OWNED, ALREADY_CLAIMED, NOT_ADJACENT, TOO_CLOSE, NOT_ALLOWED
    }

    open fun claimChunk(ownerID: UUID, x: Int, z: Int): CampClaimResult =
        when (doClaim(ownerID, ownerID, x, z)) {
            ClaimResult.SUCCESS -> CampClaimResult.SUCCESS
            ClaimResult.ALREADY_OWNED -> CampClaimResult.ALREADY_OWNED
            ClaimResult.ALREADY_TAKEN -> CampClaimResult.ALREADY_CLAIMED
            ClaimResult.NOT_ADJACENT -> CampClaimResult.NOT_ADJACENT
            ClaimResult.NOT_ALLOWED -> CampClaimResult.NOT_ALLOWED
            ClaimResult.TOO_CLOSE -> CampClaimResult.TOO_CLOSE
        }

    open fun unclaimChunk(ownerID: UUID, x: Int, z: Int): Boolean =
        when (doUnclaim(ownerID, ownerID, x, z)) {
            UnclaimResult.SUCCESS -> true
            UnclaimResult.NOT_OWNED -> false
            UnclaimResult.NOT_ALLOWED -> false
            UnclaimResult.LAST_CHUNK -> false
            UnclaimResult.IS_SPAWN_CHUNK -> false
        }

    @Transactional
    open fun trustPlayer(ownerID: UUID, trustedPlayerID: UUID): Boolean {
        val camp = repository.getById(ownerID) ?: return false
        val success = camp.addTrustedPlayer(trustedPlayerID)
        if (success) {
            logger.info("Player $trustedPlayerID is now a trusted member of campement $ownerID")
            repository.save(camp)
        }
        return success
    }

    @Transactional
    open fun untrustPlayer(ownerID: UUID, trustedPlayerID: UUID): Boolean {
        val camp = repository.getById(ownerID) ?: return false
        val success = camp.removeTrustedPlayer(trustedPlayerID)
        if (success) {
            logger.info("Removed player $trustedPlayerID from trusted members of campement $ownerID")
            repository.save(camp)
        }
        return success
    }
}

enum class CampementSetSpawnResult { SUCCESS, INVALID_WORLD, OUTSIDE_TERRITORY, NOT_AUTHORIZED }

data class CampementCreationResult(val campement: Campement?, val reason: List<Reason>) {
    enum class Reason { InvalidWorld, NearSpawn, NearCampement, OnOtherCampement, AlreadyHasCampement, Ignored }
    companion object {
        fun failed(reasons: List<Reason>) = CampementCreationResult(null, reasons)
        fun success(campement: Campement) = CampementCreationResult(campement, emptyList())
    }
}
