package onl.tesseract.srp.service.territory

import onl.tesseract.srp.DomainEventPublisher
import onl.tesseract.srp.domain.commun.ChunkCoord
import onl.tesseract.srp.domain.commun.Coordinate
import onl.tesseract.srp.domain.territory.Territory
import onl.tesseract.srp.domain.territory.TerritoryChunk
import onl.tesseract.srp.domain.territory.enum.*
import onl.tesseract.srp.repository.generic.territory.TerritoryChunkRepository
import onl.tesseract.srp.repository.generic.territory.TerritoryRepository
import onl.tesseract.srp.util.InteractionAllowResult
import java.util.*

const val TERRITORY_PROXIMITY_CLAIM_LIMIT = 3
const val TERRITORY_PROXIMITY_CREATE_LIMIT = 50

abstract class TerritoryService<TC : TerritoryChunk, T : Territory<TC>>(
) {
    protected abstract val eventService: DomainEventPublisher
    protected abstract val territoryChunkRepository: TerritoryChunkRepository
    protected abstract val territoryRepository: TerritoryRepository<T, UUID>
    /**
     * Permet de savoir si le monde est correct pour une location donnée.
     * @param worldName La position à valider
     */
    protected abstract fun isCorrectWorld(worldName: String): Boolean

    /**
     * Permet de savoir si un chunk est déjà occupé par un territoire.
     */
    protected fun isChunkTaken(chunkCoord: ChunkCoord): Boolean {
        return territoryChunkRepository.getById(chunkCoord) != null
    }

    /**
     * Permet de savoir si un chunk est déjà occupé par un autre.
     */
    protected fun isTakenByOther(territory: T, chunkCoord: ChunkCoord): Boolean {
        val territoryChunk = territoryChunkRepository.getById(chunkCoord) ?: return false
        return territoryChunk.getOwner() == territory
    }

    /**
     * Retourne le territoire qui possède ce chunkCoord.
     */
     fun getByChunk(chunkCoord: ChunkCoord): T? {
        val territoryChunk = territoryChunkRepository.getById(chunkCoord) ?:return null
        val owner = territoryChunk.getOwner()
        return try {
            @Suppress("UNCHECKED_CAST")
            owner as T
        } catch (_: Exception) {
            null
        }catch (_ : java.lang.Exception){
            null
        }
    }

    /**
     * Retourne le territoire qui possède ce chunkCoord.
     */
     fun getAnyByChunk(chunkCoord: ChunkCoord): Territory<out TerritoryChunk>? {
        val territoryChunk = territoryChunkRepository.getById(chunkCoord) ?:return null
        return territoryChunk.getOwner()
    }

    /**
     * Retourne le territoire lié au joueur.
     */
    fun getByPlayer(player: UUID): T? {
        return territoryRepository.findnByPlayer(player)
    }

    /**
     * Permet de claim un chunk
     */
    fun claimChunk(player: UUID, chunkCoord: ChunkCoord): ClaimResult {
        if (!isCorrectWorld(chunkCoord.world)) return ClaimResult.INVALID_WORLD
        val territory = getByPlayer(player) ?: return ClaimResult.TERRITORY_NOT_FOUND
        val ownerTerritory = getAnyByChunk(chunkCoord)
        if (ownerTerritory != null) {
            if (ownerTerritory != territory) return ClaimResult.ALREADY_OTHER
            return ClaimResult.ALREADY_OWNED
        }
        val tooClose = isTooCloseToOthers(chunkCoord.world, chunkCoord.x, chunkCoord.z, territory)
        if (tooClose) return ClaimResult.TOO_CLOSE
        val result = territory.claimChunk(chunkCoord, player)
        if (result == ClaimResult.SUCCESS) {
            territoryRepository.save(territory)
            eventService.publish(territory.createClaimEvent(player))
        }
        return result
    }

    /**
     * Permet de unclaim un chunk
     */
    fun unclaimChunk(player: UUID, chunkCoord: ChunkCoord): UnclaimResult {
        val territory = getByPlayer(player) ?: return UnclaimResult.TERRITORY_NOT_FOUND
        val result = territory.unclaimChunk(chunkCoord, player)
        if (result == UnclaimResult.SUCCESS) {
            territoryRepository.save(territory)
            eventService.publish(territory.createUnclaimEvent(player))
        }
        return result
    }

    /**
     * Permet de set le point de spawn
     */
    fun setSpawnpoint(player: UUID, coordinate: Coordinate): SetSpawnResult {
        val territory = getByPlayer(player) ?: return SetSpawnResult.TERRITORY_NOT_FOUND
        val result = territory.setSpawnpoint(coordinate, player)
        if (result == SetSpawnResult.SUCCESS) {
            territoryRepository.save(territory)
        }
        return result
    }

    protected abstract fun interactionOutcomeWhenNoOwner(): InteractionAllowResult
    protected abstract fun isMemberOrTrusted(territory: T, playerId: UUID): Boolean

    private fun isTooCloseToOthers(
        world: String,
        x0: Int,
        z0: Int,
        territory: T,
    ): Boolean {
        val alreadyClaimed = territoryChunkRepository.findAllByRange(
            world,
            x0 - TERRITORY_PROXIMITY_CLAIM_LIMIT,
            x0 + TERRITORY_PROXIMITY_CLAIM_LIMIT,
            z0 - TERRITORY_PROXIMITY_CLAIM_LIMIT,
            z0 + TERRITORY_PROXIMITY_CLAIM_LIMIT)
        return alreadyClaimed.any { it.getOwner() != territory }
    }

    open fun canInteractInChunk(playerId: UUID, chunk: ChunkCoord): InteractionAllowResult {
        if (!isCorrectWorld(chunk.world)) return InteractionAllowResult.Ignore
        return getByChunk(chunk)?.let { owner ->
            if (owner.canBuild(playerId)) InteractionAllowResult.Allow
            else InteractionAllowResult.Deny
        } ?: interactionOutcomeWhenNoOwner()
    }

    /**
     * Méthode core permettant de savoir si la création d'un territoire est possible
     */
    protected fun isCreationAvailable(playerID: UUID, chunkCoord: ChunkCoord): CreationResult {
        if (getByPlayer(playerID) != null) return CreationResult.ALREADY_HAS_TERRITORY
        if (!isCorrectWorld(chunkCoord.world)) return CreationResult.INVALID_WORLD
        if (isChunkTaken(chunkCoord)) return CreationResult.ON_OTHER_TERRITORY
        val x = chunkCoord.x
        val z = chunkCoord.z
        val world = chunkCoord.world
        val alreadyClaimed = territoryChunkRepository.findAllByRange(
            world,
            x - TERRITORY_PROXIMITY_CREATE_LIMIT,
            x + TERRITORY_PROXIMITY_CREATE_LIMIT,
            z - TERRITORY_PROXIMITY_CREATE_LIMIT,
            z + TERRITORY_PROXIMITY_CREATE_LIMIT)
        if (alreadyClaimed.isNotEmpty()) return CreationResult.TOO_CLOSE_TO_OTHER_TERRITORY
        return CreationResult.SUCCESS
    }

    fun getById(id: UUID): T? {
        return territoryRepository.getById(id)
    }

    /**
     * Permet de trust un joueur
     */
    fun trust(player: UUID, target: UUID): TrustResult {
        val territory = territoryRepository.findnByPlayer(player) ?: return TrustResult.TERRITORY_NOT_FOUND
        if(!territory.canTrust(player)) return TrustResult.NOT_ALLOWED
        val result = territory.addTrust(player, target)
        if (result == TrustResult.SUCCESS) {
            territoryRepository.save(territory)
        }
        return result
    }

    /**
     * Permet de untrust un joueur
     */
    fun untrust(player: UUID, target: UUID): UntrustResult {
        val territory = territoryRepository.findnByPlayer(player) ?: return UntrustResult.TERRITORY_NOT_FOUND
        if(!territory.canTrust(player)) return UntrustResult.NOT_ALLOWED
        val result = territory.removeTrust(player, target)
        if (result == UntrustResult.SUCCESS) {
            territoryRepository.save(territory)
        }
        return result
    }
}
