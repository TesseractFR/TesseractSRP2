package onl.tesseract.srp.service.territory

import onl.tesseract.srp.DomainEventPublisher
import onl.tesseract.srp.domain.commun.ChunkCoord
import onl.tesseract.srp.domain.commun.Coordinate
import onl.tesseract.srp.domain.territory.Territory
import onl.tesseract.srp.domain.territory.TerritoryChunk
import onl.tesseract.srp.domain.territory.enum.TerritoryWorld
import onl.tesseract.srp.domain.territory.enum.result.ClaimResult
import onl.tesseract.srp.domain.territory.enum.result.CreationResult
import onl.tesseract.srp.domain.territory.enum.result.SetSpawnResult
import onl.tesseract.srp.domain.territory.enum.result.TrustResult
import onl.tesseract.srp.domain.territory.enum.result.UnclaimResult
import onl.tesseract.srp.domain.territory.enum.result.UntrustResult
import onl.tesseract.srp.repository.generic.territory.TerritoryChunkRepository
import onl.tesseract.srp.repository.generic.territory.TerritoryRepository
import onl.tesseract.srp.util.InteractionAllowResult
import java.util.*

abstract class TerritoryService<TC : TerritoryChunk, T : Territory<TC>> {
    protected abstract val eventService: DomainEventPublisher
    protected abstract val territoryChunkRepository: TerritoryChunkRepository
    protected abstract val territoryRepository: TerritoryRepository<T, UUID>
    /**
     * Permet de savoir si le monde est correct pour une location donnée.
     * @param worldName La position à valider
     */
    fun isCorrectWorld(worldName: String): Boolean {
        return isCorrectWorld(getTerritoryWorld(worldName)?:return false)
    }

    abstract fun isCorrectWorld(world: TerritoryWorld): Boolean

    private fun getTerritoryWorld(world: String): TerritoryWorld? {
        return TerritoryWorld.entries.firstOrNull { it.srpWorld.bukkitName == world }
    }
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
    abstract fun getByChunk(chunkCoord: ChunkCoord): T?

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
        val world = getTerritoryWorld(chunkCoord.world)
        if (world == null || !isCorrectWorld(world)) return ClaimResult.INVALID_WORLD
        val territory = getByPlayer(player) ?: return ClaimResult.TERRITORY_NOT_FOUND
        val ownerTerritory = getAnyByChunk(chunkCoord)
        if (ownerTerritory != null) {
            if (ownerTerritory != territory) return ClaimResult.ALREADY_OTHER
            return ClaimResult.ALREADY_OWNED
        }
        val tooClose = isTooCloseToOthers(world, chunkCoord.x, chunkCoord.z, territory)
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
        world: TerritoryWorld,
        x0: Int,
        z0: Int,
        territory: T,
    ): Boolean {
        val alreadyClaimed = territoryChunkRepository.findAllByRange(
            world.srpWorld.bukkitName,
            x0 - world.claimDistance,
            x0 + world.claimDistance,
            z0 - world.claimDistance,
            z0 + world.claimDistance)
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
        val world = getTerritoryWorld(chunkCoord.world)
        if (world== null || !isCorrectWorld(chunkCoord.world)) return CreationResult.INVALID_WORLD
        if (isChunkTaken(chunkCoord)) return CreationResult.ON_OTHER_TERRITORY
        val x = chunkCoord.x
        val z = chunkCoord.z
        val alreadyClaimed = territoryChunkRepository.findAllByRange(
            chunkCoord.world,
            x - world.creationDistance,
            x + world.creationDistance,
            z - world.creationDistance,
            z + world.creationDistance)
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
