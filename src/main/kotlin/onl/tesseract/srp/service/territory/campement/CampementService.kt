package onl.tesseract.srp.service.territory.campement

import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.event.EventService
import onl.tesseract.lib.logger.LoggerFactory
import onl.tesseract.lib.service.ServiceContainer
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.domain.commun.enum.CreationResult
import onl.tesseract.srp.domain.territory.campement.Campement
import onl.tesseract.srp.domain.territory.campement.CampementChunk
import onl.tesseract.srp.domain.world.SrpWorld
import onl.tesseract.srp.repository.generic.territory.TerritoryChunkRepository
import onl.tesseract.srp.repository.hibernate.CampementRepository
import onl.tesseract.srp.service.player.SrpPlayerService
import onl.tesseract.srp.service.territory.TerritoryService
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
    private val srpPlayerService: SrpPlayerService,
    territoryChunkRepository: TerritoryChunkRepository
) : TerritoryService<CampementChunk, Campement, UUID>(repository, territoryChunkRepository, eventService) {
    @PostConstruct
    fun registerInServiceContainer() {
        ServiceContainer.getInstance().registerService(CampementService::class.java, this)
    }

    override fun isCorrectWorld(loc: Location): Boolean = worldService.getSrpWorld(loc.world) == SrpWorld.Elysea

    override fun interactionOutcomeWhenNoOwner(): InteractionAllowResult =
        InteractionAllowResult.Deny

    override fun isMemberOrTrusted(territory: Campement, playerId: UUID): Boolean {
        return territory.ownerID == playerId || territory.trustedPlayers.contains(playerId)
    }

    open fun getCampementByOwner(ownerID: UUID): Campement? {
        return repository.getById(ownerID)
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
    open fun createCampement(ownerID: UUID, spawnLocation: Location): CreationResult {
        val player = srpPlayerService.getPlayer(ownerID)

        val result = isCreationAvailable(ownerID,spawnLocation)
        if(result != CreationResult.SUCCESS) return result

        val campLevel = player.rank.campLevel
        val campement = Campement(
            ownerID = ownerID,
            campLevel = campLevel,
            spawnLocation = spawnLocation
        )
        campement.claimInitialChunks()
        logger.info("New campement (level $campLevel) created for owner $ownerID")
        repository.save(campement)
        return result
    }

    @Transactional
    open fun deleteCampement(id: UUID) {
        logger.info("Deleting campement $id")
        repository.deleteById(id)
    }

    private fun getByOwner(ownerID: UUID): Campement? {
        return repository.getById(ownerID)
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

enum class CampementSetSpawnResult { SUCCESS, OUTSIDE_TERRITORY, NOT_AUTHORIZED }

data class CampementCreationResult(val campement: Campement?, val reason: CreationResult? = null) {
    companion object {
        fun failed(reasons: CreationResult) = CampementCreationResult(null, reasons)
        fun success(campement: Campement) = CampementCreationResult(campement)
    }
}
