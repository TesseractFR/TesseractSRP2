package onl.tesseract.srp.service.territory

import onl.tesseract.srp.DomainEventPublisher
import onl.tesseract.srp.domain.territory.enum.ClaimResult
import onl.tesseract.srp.domain.territory.enum.CreationResult
import onl.tesseract.srp.domain.territory.enum.SetSpawnResult
import onl.tesseract.srp.domain.territory.enum.UnclaimResult
import onl.tesseract.srp.domain.territory.ChunkCoord
import onl.tesseract.srp.domain.territory.Coordinate
import onl.tesseract.srp.domain.territory.Territory
import onl.tesseract.srp.domain.territory.TerritoryChunk
import onl.tesseract.srp.domain.territory.enum.TrustResult
import onl.tesseract.srp.domain.territory.enum.UntrustResult
import onl.tesseract.srp.repository.generic.territory.TerritoryChunkRepository
import onl.tesseract.srp.repository.generic.territory.TerritoryRepository
import onl.tesseract.srp.util.InteractionAllowResult
import java.util.*

const val TERRITORY_PROXIMITY_CLAIM_LIMIT = 3
const val TERRITORY_PROXIMITY_CREATE_LIMIT = 50
abstract class TerritoryService<TC : TerritoryChunk,T : Territory<TC>, ID>(
    protected val eventService: DomainEventPublisher,
    ) {
    protected abstract val territoryChunkRepository: TerritoryChunkRepository
    protected abstract val territoryRepository: TerritoryRepository<T, ID>
    /**
     * Permet de savoir si le monde est correct pour une location donnée.
     * @param worldName La position à valider
     */
    protected abstract fun isCorrectWorld(worldName : String): Boolean

    /**
     * Permet de savoir si un chunk est déjà occupé par un territoire.
     */
    protected fun isChunkTaken(chunkCoord: ChunkCoord): Boolean{
        return territoryChunkRepository.getById(chunkCoord) != null
    }

    /**
     * Permet de savoir si un chunk est déjà occupé par un autre.
     */
    protected fun isTakenByOther(territory : T, chunkCoord: ChunkCoord): Boolean{
        val territoryChunk = territoryChunkRepository.getById(chunkCoord) ?:return false
        return territoryChunk.getOwner() == territory
    }
    /**
     * Retourne le territoire qui possède ce chunkCoord.
     */
     fun getByChunk(chunkCoord: ChunkCoord): T? {
        val territoryChunk = territoryChunkRepository.getById(chunkCoord) ?:return null
        return territoryChunk.getOwner() as T?
    }

    /**
     * Retourne le territoire lié au joueur.
     */
    fun getByPlayer(player: UUID) : T?{
        return territoryRepository.findnByPlayer(player)
    }

    /**
     * Permet de claim un chunk
     */
    fun claimChunk(player: UUID, chunkCoord: ChunkCoord): ClaimResult {
        if(!isCorrectWorld(chunkCoord.world)) return ClaimResult.INVALID_WORLD
        val territory = getByPlayer(player) ?: return ClaimResult.TERRITORY_NOT_FOUND
        val ownerTerritory = getByChunk(chunkCoord)
        if(ownerTerritory!=null){
            if(ownerTerritory != territory) return ClaimResult.ALREADY_OTHER
            return ClaimResult.ALREADY_OWNED
        }
        val tooClose = isTooCloseToOthers(chunkCoord.x, chunkCoord.z, TERRITORY_PROXIMITY_CLAIM_LIMIT) { cx, cz ->
                    isTakenByOther(territory, ChunkCoord(cx,cz,chunkCoord.world))
                }
        if (tooClose)return ClaimResult.TOO_CLOSE
        val result = territory.claimChunk(chunkCoord,player)
        if(result == ClaimResult.SUCCESS){
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
        val result = territory.unclaimChunk(chunkCoord,player)
        if(result == UnclaimResult.SUCCESS){
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
        val result = territory.setSpawnpoint(coordinate,player)
        if(result == SetSpawnResult.SUCCESS){
            territoryRepository.save(territory)
        }
        return result
    }

    protected abstract fun interactionOutcomeWhenNoOwner(): InteractionAllowResult
    protected abstract fun isMemberOrTrusted(territory: T, playerId: UUID): Boolean

    private fun isTooCloseToOthers(
        x0: Int,
        z0: Int,
        r: Int,
        isOtherTaken: (Int, Int) -> Boolean
    ): Boolean {
        for (dx in -r..r) for (dz in -r..r) {
            if (dx == 0 && dz == 0) continue
            if (isOtherTaken(x0 + dx, z0 + dz)) return true
        }
        return false
    }

    open fun canInteractInChunk(playerId: UUID, chunk: ChunkCoord): InteractionAllowResult {
        if (!isCorrectWorld(chunk.world)) return InteractionAllowResult.Ignore
        return getByChunk(chunk)?.let { owner ->
            if (isMemberOrTrusted(owner, playerId)) InteractionAllowResult.Allow
            else InteractionAllowResult.Deny
        } ?: interactionOutcomeWhenNoOwner()
    }

    /**
     * Méthode core permettant de savoir si la création d'un territoire est possible
     */
    protected fun isCreationAvailable(playerID: UUID, chunkCoord: ChunkCoord): CreationResult{
        if(getByPlayer(playerID) != null) return CreationResult.ALREADY_HAS_TERRITORY
        if(!isCorrectWorld(chunkCoord.world)) return CreationResult.INVALID_WORLD
        if(isChunkTaken(chunkCoord)) return CreationResult.ON_OTHER_TERRITORY
        val x = chunkCoord.x
        val z = chunkCoord.z
        val world = chunkCoord.world
        for (dx in -TERRITORY_PROXIMITY_CREATE_LIMIT..TERRITORY_PROXIMITY_CREATE_LIMIT)
            for (dz in -TERRITORY_PROXIMITY_CREATE_LIMIT..TERRITORY_PROXIMITY_CREATE_LIMIT) {
                if (dx == 0 && dz == 0) continue
                if (isChunkTaken(ChunkCoord(x + dx, z + dz, world))) return CreationResult.TOO_CLOSE_TO_OTHER_TERRITORY
        }
        return CreationResult.SUCCESS
    }

    fun getById(id: ID): T?{
        return territoryRepository.getById(id)
    }

    /**
     * Permet de trust un joueur
     */
    fun trust(player: UUID, target: UUID): TrustResult {
        val territory = territoryRepository.findnByPlayer(player) ?: return TrustResult.TERRITORY_NOT_FOUND
        val result = territory.addTrust(player,target)
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
        val result = territory.removeTrust(player,target)
        if (result == UntrustResult.SUCCESS) {
            territoryRepository.save(territory)
        }
        return result
    }
}
