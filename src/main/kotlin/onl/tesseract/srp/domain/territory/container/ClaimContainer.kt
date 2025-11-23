package onl.tesseract.srp.domain.territory.container

import onl.tesseract.srp.domain.commun.ChunkCoord
import onl.tesseract.srp.domain.territory.TerritoryChunk
import onl.tesseract.srp.domain.territory.enum.ClaimResult
import onl.tesseract.srp.domain.territory.enum.UnclaimResult
import onl.tesseract.srp.domain.territory.event.TerritoryClaimEvent
import onl.tesseract.srp.domain.territory.event.TerritoryUnclaimEvent
import java.util.*
import kotlin.math.abs


abstract class ClaimContainer<TC : TerritoryChunk>{
    protected val _chunks: MutableSet<TC> = mutableSetOf()
    private fun addChunk(chunk: TC): Boolean {
        return _chunks.add(chunk)
    }

    fun claimChunk(chunkCoord: ChunkCoord, claimer: UUID): ClaimResult {
        if (!canClaim(claimer))return ClaimResult.NOT_ALLOWED
        if (hasChunk(chunkCoord)) return ClaimResult.ALREADY_OWNED
        if (!hasAdjacent(chunkCoord)) return ClaimResult.NOT_ADJACENT
        if (addChunk(initChunk(chunkCoord))) return ClaimResult.SUCCESS
        return ClaimResult.ALREADY_OWNED
    }

    fun hasChunk(chunkCoord: ChunkCoord): Boolean {
        return _chunks.any {
            it.chunkCoord == chunkCoord
        }
    }

    fun hasAdjacent(chunkCoord: ChunkCoord): Boolean{
        val lx = chunkCoord.x
        val lz = chunkCoord.z
        return _chunks.any { n ->
            val nx = n.chunkCoord.x
            val nz = n.chunkCoord.z
            abs(nx - lx) + abs(nz - lz) == 1
        }
    }

    private fun removeChunk(chunk: TC): Boolean {
        return _chunks.remove(chunk)
    }

    abstract fun initChunk(chunkCoord: ChunkCoord): TC

    open fun unclaimChunk(chunkCoord: ChunkCoord, player : UUID): UnclaimResult {
        if(!canClaim(player)) return UnclaimResult.NOT_ALLOWED
        if (!hasChunk(chunkCoord)) return UnclaimResult.NOT_OWNED
        if (_chunks.size == 1) return UnclaimResult.LAST_CHUNK
        val chunk = _chunks.first { it.chunkCoord == chunkCoord }
        if(!isUnclaimStillConnected(chunk)) return UnclaimResult.SPLIT
        if (removeChunk(chunk)) return UnclaimResult.SUCCESS
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