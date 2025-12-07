package onl.tesseract.srp.infrastructure.runnable.territory


import onl.tesseract.srp.domain.commun.ChunkCoord
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import kotlin.ranges.step


private const val CHUNK_SIZE = 16
private const val MAX_DISTANCE: Int = 50
private const val MAX_DISTANCE_SQ: Int = MAX_DISTANCE * MAX_DISTANCE
private const val VERTICAL_STEP: Int = 2
private const val HORIZONTAL_STEP: Int = 1
private val PARTICLE: Particle = Particle.END_ROD
private const val PARTICLE_COUNT: Int = 1
private const val OFFSET: Double = 0.1
private const val EXTRA: Double = 0.01


class TerritoryBorderTask(
    private val player: Player,
    private val chunkCollection: Collection<ChunkCoord>
) : BukkitRunnable() {
    override fun run() {
        if (!player.isOnline) {
            cancel()
            return
        }
        drawBordersOnce(player, chunkCollection)
    }


    private fun spawnVerticalBorder(blockX: Int, startZ: Int, endZ: Int, minY: Int, maxY: Int, startLoc : Location) {
        for (bz in startZ..endZ step HORIZONTAL_STEP) {
            for (by in minY..maxY step VERTICAL_STEP) {
                val pos = startLoc.clone().apply { set(blockX.toDouble(), by.toDouble(), bz.toDouble()) }
                if (pos.distanceSquared(startLoc) <= MAX_DISTANCE_SQ) {
                    player.spawnParticle(
                        PARTICLE, pos, PARTICLE_COUNT,
                        OFFSET, OFFSET, OFFSET, EXTRA
                    )
                }
            }
        }
    }

    private fun spawnHorizontalBorder(startX: Int, endX: Int, blockZ: Int, minY: Int, maxY: Int, startLoc : Location) {
        for (bx in startX..endX step HORIZONTAL_STEP) {
            for (by in minY..maxY step VERTICAL_STEP) {
                val pos = startLoc.clone().apply { set(bx.toDouble(), by.toDouble(), blockZ.toDouble()) }
                if (pos.distanceSquared(startLoc) <= MAX_DISTANCE_SQ) {
                    player.spawnParticle(
                        PARTICLE, pos, PARTICLE_COUNT,
                        OFFSET, OFFSET, OFFSET, EXTRA
                    )
                }
            }
        }
    }

    /**
     * Draws borders around specified chunks for a player using particles.
     *
     * @param player The player who will see the borders.
     * @param chunks The collection of chunks to draw borders around.
     */
    fun drawBordersOnce(
        player: Player,
        chunks: Collection<ChunkCoord>
    ) {
        val set = chunks.map { it.x to it.z }.toHashSet()
        val world = player.world
        val minY = world.minHeight
        val maxY = world.maxHeight
        val ploc = player.location

        for ((cx, cz) in set) {
            val hasLeft   = (cx - 1 to cz) in set
            val hasRight  = (cx + 1 to cz) in set
            val hasTop    = (cx to cz - 1) in set
            val hasBottom = (cx to cz + 1) in set

            val minX = cx * CHUNK_SIZE
            val minZ = cz * CHUNK_SIZE
            val maxX = minX + CHUNK_SIZE
            val maxZ = minZ + CHUNK_SIZE

            if (!hasLeft)   spawnVerticalBorder(minX, minZ, maxZ,minY,maxY,ploc)
            if (!hasRight)  spawnVerticalBorder(maxX, minZ, maxZ,minY,maxY,ploc)
            if (!hasTop)    spawnHorizontalBorder(minX, maxX, minZ,minY,maxY,ploc)
            if (!hasBottom) spawnHorizontalBorder(minX, maxX, maxZ,minY,maxY,ploc)
        }
    }
}
