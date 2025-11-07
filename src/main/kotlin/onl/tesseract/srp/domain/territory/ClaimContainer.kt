package onl.tesseract.srp.domain.territory

import onl.tesseract.srp.domain.commun.enum.ClaimResult
import onl.tesseract.srp.domain.commun.enum.UnclaimResult
import org.bukkit.Location
import java.util.*
import kotlin.math.abs


abstract class ClaimContainer<TC : TerritoryChunk>{
    protected val _chunks: MutableSet<TC> = mutableSetOf()
    private fun addChunk(chunk: TC): Boolean {
        return _chunks.add(chunk)
    }

    fun claimChunk(location: Location,claimer: UUID): ClaimResult {
        if (!canClaim(claimer))return ClaimResult.NOT_ALLOWED
        if (location.world.name != getValidWorld()) return ClaimResult.INVALID_WORLD
        if (hasChunk(location)) return ClaimResult.ALREADY_OWNED
        if (!hasAdjacent(location)) return ClaimResult.NOT_ADJACENT
        if (addChunk(initChunk(location))) ClaimResult.SUCCESS
        return ClaimResult.ALREADY_OWNED
    }

    abstract fun getValidWorld() : String

    fun hasChunk(location: Location): Boolean {
        return _chunks.any {
            it.chunkCoord.world == location.world.name
                    && it.chunkCoord.z == location.chunk.z
                    && it.chunkCoord.x == location.chunk.x
        }
    }

    fun hasAdjacent(location: Location): Boolean{
        val lx = location.chunk.x
        val lz = location.chunk.z
        return _chunks.any { n ->
            val nx = n.chunkCoord.x
            val nz = n.chunkCoord.z
            abs(nx - lx) + abs(nz - lz) == 1
        }
    }

    private fun removeChunk(chunk: TC): Boolean {
        return _chunks.remove(chunk)
    }

    abstract fun initChunk(location: Location): TC

    open fun unclaimChunk(location: Location, player : UUID): UnclaimResult {
        if(!canClaim(player)) return UnclaimResult.NOT_ALLOWED
        if (!hasChunk(location)) return UnclaimResult.NOT_OWNED
        if (_chunks.size == 1) return UnclaimResult.LAST_CHUNK
        if(!isUnclaimStillConnected(initChunk(location))) return UnclaimResult.SPLIT
        if (removeChunk(initChunk(location))) UnclaimResult.SUCCESS
        return UnclaimResult.NOT_OWNED

    }

    private fun isUnclaimStillConnected(toRemove: TC): Boolean {
        val remaining = _chunks.filter { it != toRemove }
        if (remaining.isEmpty()) return false

        fun neighbors(a: TC, b: TC): Boolean {
            val (x1, z1) = a.chunkCoord.x to a.chunkCoord.z
            val (x2, z2) = b.chunkCoord.x to b.chunkCoord.z
            return abs(x1 - x2) + abs(z1 - z2) == 1
        }

        val visited = mutableSetOf<TC>()
        val queue = ArrayDeque<TC>()
        visited += remaining.first()
        queue += remaining.first()
        while (queue.isNotEmpty()) {
            val cur = queue.removeFirst()
            remaining.filter { it !in visited && neighbors(cur, it) }.forEach {
                visited += it; queue += it
            }
        }
        return visited.size == remaining.size
    }

    fun getChunks(): Set<TC> {
        return _chunks.toSet()
    }

    fun addChunks(toAdd : Collection<TC>){
        _chunks.addAll(toAdd)
    }

    abstract fun canClaim(player: UUID): Boolean

    abstract fun createClaimEvent(player: UUID): TerritoryClaimEvent<TC>

    abstract fun createUnclaimEvent(player: UUID): TerritoryUnclaimEvent<TC>
}