package onl.tesseract.srp.service.territory

import onl.tesseract.lib.event.EventService
import onl.tesseract.srp.domain.commun.enum.ClaimResult
import onl.tesseract.srp.domain.commun.enum.CreationResult
import onl.tesseract.srp.domain.commun.enum.SetSpawnResult
import onl.tesseract.srp.domain.commun.enum.UnclaimResult
import onl.tesseract.srp.domain.territory.ChunkCoord
import onl.tesseract.srp.domain.territory.Territory
import onl.tesseract.srp.domain.territory.TerritoryChunk
import onl.tesseract.srp.repository.generic.territory.TerritoryChunkRepository
import onl.tesseract.srp.repository.generic.territory.TerritoryRepository
import onl.tesseract.srp.util.InteractionAllowResult
import org.bukkit.Chunk
import org.bukkit.Location
import java.util.*

const val TERRITORY_PROXIMITY_CLAIM_LIMIT = 3
const val TERRITORY_PROXIMITY_CREATE_LIMIT = 50
abstract class TerritoryService<TC : TerritoryChunk,T : Territory<TC>, ID>(
    private val territoryRepository: TerritoryRepository<T,ID>,
    private val territoryChunkRepository: TerritoryChunkRepository,
    private val eventService: EventService,
    ) {

    /**
     * Permet de savoir si le monde est correct pour une location donnée.
     * @param loc La position à valider
     */
    protected abstract fun isCorrectWorld(loc: Location): Boolean

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
     * Retourne le territoire qui possède cette coordonnée.
     */
    fun getByChunk(location: Location): T? {
        return getByChunk(ChunkCoord(location))
    }
    /**
     * Retourne le territoire qui possède ce chunk.
     */
    fun getByChunk(chunk: Chunk): T? {
        return getByChunk(ChunkCoord(chunk))
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
    fun claimChunk(player: UUID, location: Location): ClaimResult {
        if(!isCorrectWorld(location)) return ClaimResult.INVALID_WORLD
        val territory = getByPlayer(player) ?: return ClaimResult.NOT_EXIST
        val ownerTerritory = getByChunk(location)
        if(ownerTerritory!=null){
            if(ownerTerritory != territory) return ClaimResult.ALREADY_OTHER
            return ClaimResult.ALREADY_OWNED
        }
        val tooClose = isTooCloseToOthers(location.chunk.x, location.chunk.z, TERRITORY_PROXIMITY_CLAIM_LIMIT) { cx, cz ->
                    isTakenByOther(territory, ChunkCoord(cx,cz,location.world.name))
                }
        if (tooClose)return ClaimResult.TOO_CLOSE
        val result = territory.claimChunk(location,player)
        if(result == ClaimResult.SUCCESS){
            territoryRepository.save(territory)
            eventService.callEvent(territory.createClaimEvent(player))
        }
        return result
    }

    /**
     * Permet de unclaim un chunk
     */
    fun unclaimChunk(player: UUID, location: Location): UnclaimResult {
        val territory = getByPlayer(player) ?: return UnclaimResult.NOT_EXIST
        val result = territory.unclaimChunk(location,player)
        if(result == UnclaimResult.SUCCESS){
            territoryRepository.save(territory)
            eventService.callEvent(territory.createUnclaimEvent(player))
        }
        return result
    }

    /**
     * Permet de set le point de spawn
     */
    fun setSpawnpoint(player: UUID, newLoc: Location): SetSpawnResult {
        val territory = getByPlayer(player) ?: return SetSpawnResult.NOT_EXIST
        val result = territory.setSpawnpoint(newLoc,player)
        if(result == SetSpawnResult.SUCCESS){
            territoryRepository.save(territory)
        }
        return result
    }

    protected abstract fun interactionOutcomeWhenNoOwner(): InteractionAllowResult
    protected abstract fun isMemberOrTrusted(territory: T, playerId: UUID): Boolean

    private fun checkFirstClaimClear(cx: Int, cz: Int, r: Int,world: String): Boolean {
        for (dx in -r..r) for (dz in -r..r) {
            if (dx == 0 && dz == 0) continue
            if (isChunkTaken(ChunkCoord(cx + dx, cz + dz,world))) return false
        }
        return true
    }

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

    open fun canInteractInChunk(playerId: UUID, chunk: Chunk): InteractionAllowResult {
        if (!isCorrectWorld(chunk.world.spawnLocation)) return InteractionAllowResult.Ignore
        return getByChunk(chunk)?.let { owner ->
            if (isMemberOrTrusted(owner, playerId)) InteractionAllowResult.Allow
            else InteractionAllowResult.Deny
        } ?: interactionOutcomeWhenNoOwner()
    }


    protected fun isCreationAvailable(playerID: UUID, location: Location): CreationResult{
        if(getByPlayer(playerID) != null) return CreationResult.ALREADY_HAS_TERRITORY
        if(isCorrectWorld(location)) return CreationResult.INVALID_WORLD
        if(isChunkTaken(ChunkCoord(location))) return CreationResult.ON_OTHER_TERRITORY
        val x = location.chunk.x
        val z = location.chunk.z
        val world = location.chunk.world.name
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
}
