package onl.tesseract.srp.service.campement

import onl.tesseract.srp.PLUGIN_INSTANCE
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.springframework.stereotype.Component

@Component
open class CampementBorderRenderer {

    private val activeBorders = mutableMapOf<Player, BukkitTask>()

    private val maxDistance = 50
    private val maxDistanceSquared = maxDistance * maxDistance
    private val verticalStep = 2
    private val horizontalStep = 1
    private val particle = Particle.END_ROD
    private val particleCount = 1
    private val offset = 0.1
    private val extra = 0.01

    /**
     * Displays the camp borders using End Rod particles.
     * @param player The player who will see the borders.
     * @param chunks List of chunk coordinates [x, z].
     */
    fun showBorders(player: Player, chunks: List<List<Int>>) {
        clearBorders(player)

        val task: BukkitTask = object : BukkitRunnable() {
            override fun run() {
                if (!player.isOnline) {
                    cancel()
                    activeBorders.remove(player)
                    return
                }
                drawCampBorders(player, chunks)
            }
        }.runTaskTimer(PLUGIN_INSTANCE, 0L, 20L * 2)

        activeBorders[player] = task
    }

    /**
     * Clears the previously displayed borders for a player.
     * @param player The player whose borders should be cleared.
     */
    fun clearBorders(player: Player) {
        activeBorders.remove(player)?.cancel()
    }

    /**
     * Draws all the outer borders of the camp.
     * @param player The player who will see the effects.
     * @param chunks List of owned chunks.
     */
    private fun drawCampBorders(player: Player, chunks: List<List<Int>>) {
        val chunkSet = chunks.map { it[0] to it[1] }.toSet()

        for ((chunkX, chunkZ) in chunkSet) {
            val minX = chunkX * 16
            val minZ = chunkZ * 16
            val maxX = minX + 16
            val maxZ = minZ + 16

            val hasLeft = chunkSet.contains(chunkX - 1 to chunkZ)
            val hasRight = chunkSet.contains(chunkX + 1 to chunkZ)
            val hasTop = chunkSet.contains(chunkX to chunkZ - 1)
            val hasBottom = chunkSet.contains(chunkX to chunkZ + 1)

            if (!hasLeft) drawVerticalBorder(player, minX, minZ, maxZ)
            if (!hasRight) drawVerticalBorder(player, maxX, minZ, maxZ)
            if (!hasTop) drawHorizontalBorder(player, minX, maxX, minZ)
            if (!hasBottom) drawHorizontalBorder(player, minX, maxX, maxZ)
        }
    }

    /**
     * Draws a vertical border using particles, covering the entire world height.
     * @param world The world where the particles should be spawned.
     * @param x The X-coordinate of the border.
     * @param startZ The starting Z-coordinate.
     * @param endZ The ending Z-coordinate.
     */
    private fun drawVerticalBorder(player: Player, x: Int, startZ: Int, endZ: Int) {
        val world = player.world
        val minY = world.minHeight
        val maxY = world.maxHeight
        val playerLocation = player.location

        for (z in startZ..endZ step horizontalStep) {
            for (y in minY..maxY step verticalStep) {
                val particleLocation = playerLocation.clone().apply {
                    set(x.toDouble(), y.toDouble(), z.toDouble())
                }

                if (particleLocation.distanceSquared(playerLocation) <= maxDistanceSquared) {
                    player.spawnParticle(particle, particleLocation, particleCount, offset, offset, offset, extra)
                }
            }
        }
    }


    /**
     * Draws a horizontal border using particles, covering the entire world height.
     * @param world The world where the particles should be spawned.
     * @param startX The starting X-coordinate.
     * @param endX The ending X-coordinate.
     * @param z The Z-coordinate of the border.
     */
    private fun drawHorizontalBorder(player: Player, startX: Int, endX: Int, z: Int) {
        val world = player.world
        val minY = world.minHeight
        val maxY = world.maxHeight
        val playerLocation = player.location

        for (x in startX..endX step horizontalStep) {
            for (y in minY..maxY step verticalStep) {
                val particleLocation = playerLocation.clone().apply {
                    set(x.toDouble(), y.toDouble(), z.toDouble())
                }

                if (particleLocation.distanceSquared(playerLocation) <= maxDistanceSquared) {
                    player.spawnParticle(particle, particleLocation, particleCount, offset, offset, offset, extra)
                }
            }
        }
    }

    /**
     * Checks if the borders are currently being displayed for a player.
     * @param player The player to check.
     * @return True if borders are active, False otherwise.
     */
    fun isShowingBorders(player: Player): Boolean {
        return activeBorders.containsKey(player)
    }

}


