package onl.tesseract.srp.service.territory.campement

import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import onl.tesseract.lib.logger.LoggerFactory
import onl.tesseract.lib.service.ServiceContainer
import onl.tesseract.srp.DomainEventPublisher
import onl.tesseract.srp.domain.territory.enum.CreationResult
import onl.tesseract.srp.domain.territory.Coordinate
import onl.tesseract.srp.domain.territory.campement.Campement
import onl.tesseract.srp.domain.territory.campement.CampementChunk
import onl.tesseract.srp.domain.world.SrpWorld
import onl.tesseract.srp.repository.generic.territory.TerritoryChunkRepository
import onl.tesseract.srp.repository.hibernate.CampementRepository
import onl.tesseract.srp.service.player.SrpPlayerService
import onl.tesseract.srp.service.territory.TerritoryService
import onl.tesseract.srp.service.world.WorldService
import onl.tesseract.srp.util.InteractionAllowResult
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
    eventService: DomainEventPublisher,
    private val worldService: WorldService,
    private val srpPlayerService: SrpPlayerService,
    territoryChunkRepository: TerritoryChunkRepository
) : TerritoryService<CampementChunk, Campement, UUID>(repository, territoryChunkRepository, eventService) {
    @PostConstruct
    fun registerInServiceContainer() {
        ServiceContainer.getInstance().registerService(CampementService::class.java, this)
    }

    override fun isCorrectWorld(worldName: String): Boolean = SrpWorld.Elysea.name == worldName

    override fun interactionOutcomeWhenNoOwner(): InteractionAllowResult =
        InteractionAllowResult.Deny

    override fun isMemberOrTrusted(territory: Campement, playerId: UUID): Boolean {
        return territory.ownerID == playerId || territory.isTrusted(playerId)
    }

    open fun getCampementByOwner(ownerID: UUID): Campement? {
        return repository.getById(ownerID)
    }

    open fun getAllCampements(): List<Campement> = repository.findAll()

    open fun hasCampement(sender: UUID): Boolean {
        return super.getById(sender) != null
        //TODO REMOVE ?
//        val has = getCampementByOwner(sender.uniqueId) != null
//        if (!has) {
//            sender.sendMessage(CampementChatError + "Tu ne possèdes pas de campement. Utilise "
//                    + Component.text("/campement create", NamedTextColor.GOLD)
//                    + Component.text(" pour en créer un !"))
//        }
    }

    @Transactional
    open fun createCampement(ownerID: UUID, spawnLocation: Coordinate): CreationResult {
        val player = srpPlayerService.getPlayer(ownerID)

        val result = isCreationAvailable(ownerID,spawnLocation.chunkCoord)
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

    open fun getCampSpawn(ownerID: UUID): Coordinate? = repository.getById(ownerID)?.getSpawnpoint()

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
}