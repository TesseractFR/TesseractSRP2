package onl.tesseract.srp.service.territory.campement

import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import onl.tesseract.lib.logger.LoggerFactory
import onl.tesseract.lib.service.ServiceContainer
import onl.tesseract.srp.DomainEventPublisher
import onl.tesseract.srp.domain.commun.ChunkCoord
import onl.tesseract.srp.domain.commun.Coordinate
import onl.tesseract.srp.domain.territory.campement.Campement
import onl.tesseract.srp.domain.territory.campement.CampementChunk
import onl.tesseract.srp.domain.territory.enum.TerritoryWorld
import onl.tesseract.srp.domain.territory.enum.result.CreationResult
import onl.tesseract.srp.repository.generic.territory.TerritoryChunkRepository
import onl.tesseract.srp.repository.hibernate.CampementRepository
import onl.tesseract.srp.service.player.SrpPlayerService
import onl.tesseract.srp.service.territory.TerritoryService
import onl.tesseract.srp.util.InteractionAllowResult
import org.slf4j.Logger
import org.springframework.stereotype.Service
import java.util.*

private val logger: Logger = LoggerFactory.getLogger(CampementService::class.java)

@Service
open class CampementService(
    override val territoryRepository: CampementRepository,
    override val territoryChunkRepository: TerritoryChunkRepository,
    override val eventService: DomainEventPublisher,
    private val srpPlayerService: SrpPlayerService
) : TerritoryService<CampementChunk, Campement>() {
    @PostConstruct
    fun registerInServiceContainer() {
        ServiceContainer.getInstance().registerService(CampementService::class.java, this)
    }

    override fun isCorrectWorld(world: TerritoryWorld): Boolean {
        return world == TerritoryWorld.ELYSEA
    }

    override fun getByChunk(chunkCoord: ChunkCoord): Campement? {
        val territoryChunk = territoryChunkRepository.getById(chunkCoord) ?:return null
        val owner = territoryChunk.getOwner()
        if(owner !is Campement) return null
        return owner as? Campement?
    }

    override fun interactionOutcomeWhenNoOwner(): InteractionAllowResult =
        InteractionAllowResult.Deny

    override fun isMemberOrTrusted(territory: Campement, playerId: UUID): Boolean {
        return territory.ownerID == playerId || territory.isTrusted(playerId)
    }

    open fun getCampementByOwner(ownerID: UUID): Campement? {
        return territoryRepository.getById(ownerID)
    }

    open fun getAllCampements(): List<Campement> = territoryRepository.findAll()

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
        territoryRepository.save(campement)
        return result
    }

    @Transactional
    open fun deleteCampement(id: UUID) {
        logger.info("Deleting campement $id")
        territoryRepository.deleteById(id)
    }

    private fun getByOwner(ownerID: UUID): Campement? {
        return territoryRepository.getById(ownerID)
    }

    open fun getCampSpawn(ownerID: UUID): Coordinate? = territoryRepository.getById(ownerID)?.getSpawnpoint()

    /**
     * Increments the level of the player's camp.
     * @param ownerID The UUID of the player who owns the camp.
     * @return The new camp level if successful, or null if the camp does not exist.
     */
    @Transactional
    open fun setCampLevel(ownerID: UUID, level: Int): Boolean {
        val campement = territoryRepository.getById(ownerID)
            ?: throw IllegalArgumentException("Campement from $ownerID does not exist")

        if (campement.campLevel != level) {
            campement.campLevel = level
            logger.info("Campement level from $ownerID set to $level")
            territoryRepository.save(campement)
            return true
        }
        return false
    }
}
