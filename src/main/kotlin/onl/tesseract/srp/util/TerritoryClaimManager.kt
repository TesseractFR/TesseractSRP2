package onl.tesseract.srp.util

import kotlin.math.abs

enum class ClaimResult { SUCCESS, ALREADY_OWNED, ALREADY_TAKEN, NOT_ADJACENT, NOT_ALLOWED, TOO_CLOSE }
enum class UnclaimResult { SUCCESS, NOT_OWNED, NOT_ALLOWED, LAST_CHUNK, IS_SPAWN_CHUNK }

data class Policy(
    val requireAdjacent: Boolean = true,
    val allowFirstAnywhere: Boolean = true,
    val forbidLastRemoval: Boolean = true,
    val forbidSpawnRemoval: Boolean = true,
    val keepConnected: Boolean = true,
    val protectionRadius: Int = 0
)

data class ClaimOperations<T>(
    val authorized: () -> Boolean,
    val takenElsewhere: (T) -> Boolean,
    val addAndPersist: (T) -> Unit,
    val coords: (T) -> Pair<Int, Int>,
    val isTakenAt: (Int, Int) -> Boolean = { _, _ -> false }
)

data class UnclaimOperations<T>(
    val authorized: () -> Boolean,
    val isSpawnChunk: (T) -> Boolean,
    val removeAndPersist: (T) -> Unit,
    val coords: (T) -> Pair<Int, Int>
)

object TerritoryClaimManager {

    fun <T> claim(
        owned: Collection<T>,
        target: T,
        policy: Policy = Policy(),
        io: ClaimOperations<T>
    ): ClaimResult {
        val res = when {
            !io.authorized()        -> ClaimResult.NOT_ALLOWED
            owned.contains(target)  -> ClaimResult.ALREADY_OWNED
            io.takenElsewhere(target)-> ClaimResult.ALREADY_TAKEN
            else -> {
                val adjacentOk = isAdjacencyOk(owned, target, policy, io.coords)
                if (!adjacentOk) ClaimResult.NOT_ADJACENT
                else {
                    val tooClose = isTooCloseToOthers(target, policy, io.coords, io.isTakenAt)
                    if (tooClose) ClaimResult.TOO_CLOSE
                    else {
                        io.addAndPersist(target)
                        ClaimResult.SUCCESS
                    }
                }
            }
        }
        return res
    }

    fun <T> unclaim(
        owned: Collection<T>,
        target: T,
        policy: Policy = Policy(),
        io: UnclaimOperations<T>
    ): UnclaimResult {
        val res = when {
            !io.authorized() -> UnclaimResult.NOT_ALLOWED
            !owned.contains(target) -> UnclaimResult.NOT_OWNED
            policy.forbidLastRemoval && owned.size == 1 -> UnclaimResult.LAST_CHUNK
            policy.forbidSpawnRemoval && io.isSpawnChunk(target) -> UnclaimResult.IS_SPAWN_CHUNK
            policy.keepConnected && !isUnclaimValid(owned, target, io.coords) -> UnclaimResult.NOT_ALLOWED
            else -> {
                io.removeAndPersist(target)
                UnclaimResult.SUCCESS
            }
        }
        return res
    }

    private fun <T> isAdjacencyOk(
        owned: Collection<T>,
        target: T,
        policy: Policy,
        coords: (T) -> Pair<Int, Int>
    ): Boolean {
        val adjacencyRequired = policy.requireAdjacent
        val firstClaim = owned.isEmpty()

        val result = when {
            !adjacencyRequired -> true
            firstClaim -> policy.allowFirstAnywhere
            else -> owned.any { existing ->
                val (x1, z1) = coords(existing)
                val (x2, z2) = coords(target)
                abs(x1 - x2) + abs(z1 - z2) == 1
            }
        }
        return result
    }

    fun <T> isUnclaimValid(
        owned: Collection<T>,
        toRemove: T,
        coords: (T) -> Pair<Int, Int>
    ): Boolean {
        val remaining = owned.filter { it != toRemove }
        if (remaining.isEmpty()) return false

        fun neighbors(a: T, b: T) =
            abs(coords(a).first - coords(b).first) + abs(coords(a).second - coords(b).second) == 1

        val visited = mutableSetOf<T>()
        val queue = ArrayDeque<T>()
        visited += remaining.first()
        queue += remaining.first()

        while (queue.isNotEmpty()) {
            val cur = queue.removeFirst()
            remaining.filter { it !in visited && neighbors(cur, it) }.forEach {
                visited += it
                queue += it
            }
        }
        return visited.size == remaining.size
    }

    private fun <T> isTooCloseToOthers(
        target: T,
        policy: Policy,
        coords: (T) -> Pair<Int, Int>,
        isTakenAt: (Int, Int) -> Boolean
    ): Boolean {
        val r = policy.protectionRadius
        val (x0, z0) = coords(target)
        return r > 0 && (-r..r).asSequence()
            .flatMap { dx -> (-r..r).asSequence().map { dz -> dx to dz } }
            .any { (dx, dz) -> (dx != 0 || dz != 0) && isTakenAt(x0 + dx, z0 + dz) }
    }

}