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


    protected abstract val spawnProtectionRadius: Int
    protected abstract val territoryProtectionRadius: Int

    protected abstract fun isCorrectWorld(loc: Location): Boolean

    protected fun isChunkTaken(chunkCoord: ChunkCoord): Boolean{
        return territoryChunkRepository.getById(chunkCoord) != null
    }
    protected fun isTakenByOther(territory : T, chunkCoord: ChunkCoord): Boolean{
        val territoryChunk = territoryChunkRepository.getById(chunkCoord) ?:return false
        return territoryChunk.getOwner() == territory
    }

    open fun getByChunk(location: Location): T? {
        return getByChunk(ChunkCoord(location))
    }
    open fun getByChunk(chunk: Chunk): T? {
        return getByChunk(ChunkCoord(chunk))
    }
     open fun getByChunk(chunkCoord: ChunkCoord): T? {
        val territoryChunk = territoryChunkRepository.getById(chunkCoord) ?:return null
        return territoryChunk.getOwner() as T?
    }

    open fun getByPlayer(player: UUID) : T?{
        return territoryRepository.findnByPlayer(player)
    }

    protected abstract fun isAuthorizedToSetSpawn(territory: T, requesterId: UUID): Boolean

    protected fun persistSpawn(territory: T, loc: Location): Boolean{
        val ok = territory.setSpawnpoint(loc)
        if (ok) territoryRepository.save(territory)
        return ok
    }

    fun claimChunk(player: UUID, location: Location): ClaimResult {
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

    fun unclaimChunk(player: UUID, location: Location): UnclaimResult {
        val territory = getByPlayer(player) ?: return UnclaimResult.NOT_EXIST
        val result = territory.unclaimChunk(location,player)
        if(result == UnclaimResult.SUCCESS){
            territoryRepository.save(territory)
            eventService.callEvent(territory.createUnclaimEvent(player))
        }
        return result
    }

    fun setSpawnpoint(territory: T, requesterId: UUID, newLoc: Location): SetSpawnResult {
        val inside = territory.hasChunk(newLoc)

        val result = when {
            !isAuthorizedToSetSpawn(territory, requesterId) -> SetSpawnResult.NOT_AUTHORIZED
            !isCorrectWorld(newLoc) -> SetSpawnResult.INVALID_WORLD
            !inside -> SetSpawnResult.OUTSIDE_TERRITORY
            persistSpawn(territory, newLoc) -> SetSpawnResult.SUCCESS
            else -> SetSpawnResult.OUTSIDE_TERRITORY
        }
        return result
    }

    protected open fun interactionOutcomeWrongWorld(): InteractionAllowResult =
        InteractionAllowResult.Ignore
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
        if (!isCorrectWorld(chunk.world.spawnLocation)) return interactionOutcomeWrongWorld()
        return getByChunk(ChunkCoord(chunk.x, chunk.z,chunk.world.name))?.let { owner ->
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
}
