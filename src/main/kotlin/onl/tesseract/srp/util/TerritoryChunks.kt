package onl.tesseract.srp.util

import onl.tesseract.srp.PLUGIN_INSTANCE
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import kotlin.math.abs

private const val CHUNK_SIZE = 16
private const val TICKS_PER_SECOND = 20L

object TerritoryChunks {

    /**
     * Check if the `target` chunk is adjacent (4-directionally) to any chunk in `existing`.
     *
     * @param existing The collection of existing chunks.
     * @param target The chunk to check for adjacency.
     * @param x Function to extract the x-coordinate from a chunk.
     * @param z Function to extract the z-coordinate from a chunk.
     * @return True if `target` is adjacent to any chunk in `existing`, false otherwise.
     */
    fun <C> isAdjacentToAny(
        existing: Collection<C>,
        target: C,
        x: (C) -> Int,
        z: (C) -> Int
    ): Boolean {
        val tx = x(target)
        val tz = z(target)
        return existing.any { e ->
            val dx = x(e) - tx
            val dz = z(e) - tz
            abs(dx) + abs(dz) == 1
        }
    }

    /**
     * Check if removing `toRemove` from `chunks` would still leave all remaining chunks connected.
     * Two chunks are considered connected if they share a side (4-directional connectivity).
     *
     * @param chunks The collection of chunks before removal.
     * @param toRemove The chunk to be removed.
     * @param x Function to extract the x-coordinate from a chunk.
     * @param z Function to extract the z-coordinate from a chunk.
     * @return True if the remaining chunks are still connected after removal, false otherwise.
     */
    fun <C> isUnclaimValid(
        chunks: Collection<C>,
        toRemove: C,
        x: (C) -> Int,
        z: (C) -> Int
    ): Boolean {
        val remaining = chunks.filter { it != toRemove }
        if (remaining.isEmpty()) return false

        fun is4Neighbor(a: C, b: C): Boolean =
            abs(x(a) - x(b)) + abs(z(a) - z(b)) == 1

        val visited = mutableSetOf<C>()
        val queue = ArrayDeque<C>()
        val start = remaining.first()
        visited += start
        queue += start

        while (queue.isNotEmpty()) {
            val cur = queue.removeFirst()
            remaining.asSequence()
                .filter { it !== cur && is4Neighbor(cur, it) && it !in visited }
                .forEach { n ->
                    visited += n
                    queue += n
                }
        }
        return visited.size == remaining.size
    }

    data class BorderRenderConfig(
        val maxDistance: Int = 50,
        val verticalStep: Int = 2,
        val horizontalStep: Int = 1,
        val particle: Particle = Particle.END_ROD,
        val particleCount: Int = 1,
        val offset: Double = 0.1,
        val extra: Double = 0.01,
        val periodTicks: Long = TICKS_PER_SECOND * 2
    )

    /**
     * Starts a repeating task to draw borders around specified chunks for a player.
     *
     * @param player The player who will see the borders.
     * @param chunksProvider A function that provides the current collection of chunks.
     * @param x Function to extract the x-coordinate from a chunk.
     * @param z Function to extract the z-coordinate from a chunk.
     * @param config Configuration for border rendering (particle type, distance, steps, etc.).
     * @param plugin The JavaPlugin instance to schedule the task with.
     * @return The BukkitTask representing the scheduled border drawing task.
     */
    fun <C> startBorderTask(
        player: Player,
        chunksProvider: () -> Collection<C>,
        x: (C) -> Int,
        z: (C) -> Int,
        config: BorderRenderConfig = BorderRenderConfig(),
        plugin: JavaPlugin = PLUGIN_INSTANCE
    ): BukkitTask {
        return object : BukkitRunnable() {
            override fun run() {
                if (!player.isOnline) {
                    cancel()
                    return
                }
                drawBordersOnce(player, chunksProvider(), x, z, config)
            }
        }.runTaskTimer(plugin, 0L, config.periodTicks)
    }

    /**
     * Draws borders around specified chunks for a player using particles.
     *
     * @param player The player who will see the borders.
     * @param chunks The collection of chunks to draw borders around.
     * @param x Function to extract the x-coordinate from a chunk.
     * @param z Function to extract the z-coordinate from a chunk.
     * @param config Configuration for border rendering (particle type, distance, steps, etc.).
     */
    fun <C> drawBordersOnce(
        player: Player,
        chunks: Collection<C>,
        x: (C) -> Int,
        z: (C) -> Int,
        config: BorderRenderConfig = BorderRenderConfig()
    ) {
        val set = chunks.map { x(it) to z(it) }.toHashSet()
        val maxDistSq = config.maxDistance * config.maxDistance
        val world = player.world
        val minY = world.minHeight
        val maxY = world.maxHeight
        val ploc = player.location

        fun spawnVerticalBorder(blockX: Int, startZ: Int, endZ: Int) {
            for (bz in startZ..endZ step config.horizontalStep) {
                for (by in minY..maxY step config.verticalStep) {
                    val pos = ploc.clone().apply { set(blockX.toDouble(), by.toDouble(), bz.toDouble()) }
                    if (pos.distanceSquared(ploc) <= maxDistSq) {
                        player.spawnParticle(
                            config.particle, pos, config.particleCount,
                            config.offset, config.offset, config.offset, config.extra
                        )
                    }
                }
            }
        }

        fun spawnHorizontalBorder(startX: Int, endX: Int, blockZ: Int) {
            for (bx in startX..endX step config.horizontalStep) {
                for (by in minY..maxY step config.verticalStep) {
                    val pos = ploc.clone().apply { set(bx.toDouble(), by.toDouble(), blockZ.toDouble()) }
                    if (pos.distanceSquared(ploc) <= maxDistSq) {
                        player.spawnParticle(
                            config.particle, pos, config.particleCount,
                            config.offset, config.offset, config.offset, config.extra
                        )
                    }
                }
            }
        }
        for ((cx, cz) in set) {
            val hasLeft   = (cx - 1 to cz) in set
            val hasRight  = (cx + 1 to cz) in set
            val hasTop    = (cx to cz - 1) in set
            val hasBottom = (cx to cz + 1) in set

            val minX = cx * CHUNK_SIZE
            val minZ = cz * CHUNK_SIZE
            val maxX = minX + CHUNK_SIZE
            val maxZ = minZ + CHUNK_SIZE

            if (!hasLeft)   spawnVerticalBorder(minX, minZ, maxZ)
            if (!hasRight)  spawnVerticalBorder(maxX, minZ, maxZ)
            if (!hasTop)    spawnHorizontalBorder(minX, maxX, minZ)
            if (!hasBottom) spawnHorizontalBorder(minX, maxX, maxZ)
        }
    }
}
