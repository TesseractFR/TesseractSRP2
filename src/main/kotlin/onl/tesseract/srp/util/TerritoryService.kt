package onl.tesseract.srp.util

import onl.tesseract.srp.domain.player.PlayerRank
import org.bukkit.Chunk
import org.bukkit.Location
import org.springframework.stereotype.Component
import java.util.*
import kotlin.math.abs

enum class ClaimResult { SUCCESS, ALREADY_OWNED, ALREADY_TAKEN, NOT_ADJACENT, NOT_ALLOWED, TOO_CLOSE }
enum class UnclaimResult { SUCCESS, NOT_OWNED, NOT_ALLOWED, LAST_CHUNK, IS_SPAWN_CHUNK }
enum class SetSpawnResult { SUCCESS, NOT_AUTHORIZED, INVALID_WORLD, OUTSIDE_TERRITORY }
enum class CreationError {
    ALREADY_HAS_TERRITORY, INVALID_WORLD, NEAR_SPAWN, NAME_TAKEN,
    NOT_ENOUGH_MONEY, RANK_TOO_LOW, TOO_CLOSE_TO_OTHER_TERRITORY, ON_OTHER_TERRITORY
}

@Component
abstract class TerritoryService<C, O> {

    protected abstract val spawnProtectionRadius: Int
    protected abstract val territoryProtectionRadius: Int

    protected abstract fun isCorrectWorld(loc: Location): Boolean

    protected abstract fun hasTerritory(ownerId: O): Boolean
    protected abstract fun isChunkTaken(x: Int, z: Int): Boolean
    protected abstract fun isTakenByOther(ownerId: O, x: Int, z: Int): Boolean

    protected abstract fun ownerOf(x: Int, z: Int): O?
    protected abstract fun getOwnedChunks(ownerId: O): MutableSet<C>

    protected abstract fun chunkOf(x: Int, z: Int): C
    protected abstract fun chunkOf(loc: Location): C
    protected abstract fun coords(c: C): Pair<Int, Int>

    protected abstract fun isAuthorizedToClaim(ownerId: O, requesterId: UUID): Boolean
    protected abstract fun isAuthorizedToUnclaim(ownerId: O, requesterId: UUID): Boolean
    protected abstract fun isAuthorizedToSetSpawn(ownerId: O, requesterId: UUID): Boolean

    protected abstract fun persistAfterClaim(ownerId: O, claimed: C)
    protected abstract fun persistAfterUnclaim(ownerId: O, unclaimed: C)
    protected abstract fun persistSpawn(ownerId: O, loc: Location): Boolean
    protected abstract fun isSpawnChunk(ownerId: O, c: C): Boolean

    protected fun performCreationChecks(
        ownerId: O,
        location: Location,
        playerMoney: Int?,
        playerRank: PlayerRank?,
        minMoney: Int?,
        minRank: PlayerRank?,
        alreadyHas: Boolean,
        isNameTaken: Boolean? = null
    ): List<CreationError> {
        val errors = mutableListOf<CreationError>()

        if (alreadyHas || hasTerritory(ownerId)) {
            errors += CreationError.ALREADY_HAS_TERRITORY
        } else if (!isCorrectWorld(location)) {
            errors += CreationError.INVALID_WORLD
        } else {
            if (spawnProtectionRadius > 0) {
                val spawn = location.world.spawnLocation
                if (location.distance(spawn) <= spawnProtectionRadius) {
                    errors += CreationError.NEAR_SPAWN
                }
            }
            val (cx, cz) = location.chunk.let { it.x to it.z }
            if (isChunkTaken(cx, cz)) {
                errors += CreationError.ON_OTHER_TERRITORY
            } else if (territoryProtectionRadius > 0 && !checkFirstClaimClear(cx, cz, territoryProtectionRadius)) {
                errors += CreationError.TOO_CLOSE_TO_OTHER_TERRITORY
            }
            if (isNameTaken == true) errors += CreationError.NAME_TAKEN
            if (minMoney != null && (playerMoney ?: Int.MAX_VALUE) < minMoney) errors += CreationError.NOT_ENOUGH_MONEY
            if (minRank != null && (playerRank ?: PlayerRank.Survivant) < minRank) errors += CreationError.RANK_TOO_LOW
        }
        return errors
    }

    protected fun doClaim(ownerId: O, requesterId: UUID, x: Int, z: Int): ClaimResult {
        val owned: Set<C> = getOwnedChunks(ownerId).toSet()
        val target = chunkOf(x, z)
        val (tx, tz) = coords(target)

        val firstClaim = owned.isEmpty()
        val adjacencyOk = when {
            !firstClaim && requireAdjacent() -> owned.any { n ->
                val (nx, nz) = coords(n)
                abs(nx - tx) + abs(nz - tz) == 1
            }
            firstClaim -> allowFirstAnywhere()
            else -> true
        }

        val tooClose = territoryProtectionRadius > 0 &&
                isTooCloseToOthers(tx, tz, territoryProtectionRadius) { cx, cz ->
                    isTakenByOther(ownerId, cx, cz)
                }

        val result = when {
            !isAuthorizedToClaim(ownerId, requesterId) -> ClaimResult.NOT_ALLOWED
            owned.contains(target) -> ClaimResult.ALREADY_OWNED
            isTakenByOther(ownerId, tx, tz) -> ClaimResult.ALREADY_TAKEN
            !adjacencyOk -> ClaimResult.NOT_ADJACENT
            tooClose -> ClaimResult.TOO_CLOSE
            else -> {
                persistAfterClaim(ownerId, target)
                ClaimResult.SUCCESS
            }
        }
        return result
    }

    protected fun doUnclaim(ownerId: O, requesterId: UUID, x: Int, z: Int): UnclaimResult {
        val owned: Set<C> = getOwnedChunks(ownerId).toSet()
        val target = chunkOf(x, z)
        val lastChunk = forbidLastRemoval() && owned.size == 1
        val isSpawn = forbidSpawnRemoval() && isSpawnChunk(ownerId, target)
        val breaksConnectivity = keepConnected() && !isUnclaimStillConnected(owned, target)

        val result = when {
            !isAuthorizedToUnclaim(ownerId, requesterId) -> UnclaimResult.NOT_ALLOWED
            !owned.contains(target) -> UnclaimResult.NOT_OWNED
            lastChunk -> UnclaimResult.LAST_CHUNK
            isSpawn -> UnclaimResult.IS_SPAWN_CHUNK
            breaksConnectivity -> UnclaimResult.NOT_ALLOWED
            else -> {
                persistAfterUnclaim(ownerId, target)
                UnclaimResult.SUCCESS
            }
        }
        return result
    }

    protected fun doSetSpawn(ownerId: O, requesterId: UUID, newLoc: Location): SetSpawnResult {
        val owned: Set<C> = getOwnedChunks(ownerId).toSet()
        val inside = owned.contains(chunkOf(newLoc))

        val result = when {
            !isAuthorizedToSetSpawn(ownerId, requesterId) -> SetSpawnResult.NOT_AUTHORIZED
            !isCorrectWorld(newLoc) -> SetSpawnResult.INVALID_WORLD
            !inside -> SetSpawnResult.OUTSIDE_TERRITORY
            persistSpawn(ownerId, newLoc) -> SetSpawnResult.SUCCESS
            else -> SetSpawnResult.OUTSIDE_TERRITORY
        }
        return result
    }

    protected open fun requireAdjacent(): Boolean = true
    protected open fun allowFirstAnywhere(): Boolean = true
    protected open fun forbidLastRemoval(): Boolean = true
    protected open fun forbidSpawnRemoval(): Boolean = true
    protected open fun keepConnected(): Boolean = true

    protected open fun interactionOutcomeWrongWorld(): InteractionAllowResult =
        InteractionAllowResult.Ignore
    protected abstract fun interactionOutcomeWhenNoOwner(): InteractionAllowResult
    protected abstract fun isMemberOrTrusted(ownerId: O, playerId: UUID): Boolean

    private fun checkFirstClaimClear(cx: Int, cz: Int, r: Int): Boolean {
        for (dx in -r..r) for (dz in -r..r) {
            if (dx == 0 && dz == 0) continue
            if (isChunkTaken(cx + dx, cz + dz)) return false
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

    private fun isUnclaimStillConnected(owned: Collection<C>, toRemove: C): Boolean {
        val remaining = owned.filter { it != toRemove }
        if (remaining.isEmpty()) return false

        fun neighbors(a: C, b: C): Boolean {
            val (x1, z1) = coords(a); val (x2, z2) = coords(b)
            return abs(x1 - x2) + abs(z1 - z2) == 1
        }

        val visited = mutableSetOf<C>()
        val queue = ArrayDeque<C>()
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

    open fun canInteractInChunk(playerId: UUID, chunk: Chunk): InteractionAllowResult {
        if (!isCorrectWorld(chunk.world.spawnLocation)) return interactionOutcomeWrongWorld()
        return ownerOf(chunk.x, chunk.z)?.let { owner ->
            if (isMemberOrTrusted(owner, playerId)) InteractionAllowResult.Allow
            else InteractionAllowResult.Deny
        } ?: interactionOutcomeWhenNoOwner()
    }
}
