package onl.tesseract.srp.service.campement

import onl.tesseract.srp.PLUGIN_INSTANCE
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.springframework.stereotype.Component

@Component
class CampementBorderRenderer {

    private val activeBorders = mutableMapOf<Player, BukkitTask>()

    /**
     * Displays the camp borders using End Rod particles.
     * @param player The player who will see the borders.
     * @param chunks List of chunk coordinates [x, z].
     */
    fun showBorders(player: Player, chunks: List<List<Int>>) {
        clearBorders(player)

        val task: BukkitTask = object : BukkitRunnable() {
            override fun run() {
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
        activeBorders[player]?.cancel()
        activeBorders.remove(player)
    }

    /**
     * Draws all the outer borders of the camp.
     * @param player The player who will see the effects.
     * @param chunks List of owned chunks.
     */
    private fun drawCampBorders(player: Player, chunks: List<List<Int>>) {
        val world = player.world
        val chunkSet = chunks.map { it[0] to it[1] }.toSet() // Convert to Set for quick lookup

        for ((chunkX, chunkZ) in chunkSet) {
            val minX = chunkX * 16
            val minZ = chunkZ * 16
            val maxX = minX + 16
            val maxZ = minZ + 16

            val hasLeft = chunkSet.contains(chunkX - 1 to chunkZ)
            val hasRight = chunkSet.contains(chunkX + 1 to chunkZ)
            val hasTop = chunkSet.contains(chunkX to chunkZ - 1)
            val hasBottom = chunkSet.contains(chunkX to chunkZ + 1)

            if (!hasLeft) drawVerticalBorder(world, minX, minZ, maxZ)
            if (!hasRight) drawVerticalBorder(world, maxX, minZ, maxZ)
            if (!hasTop) drawHorizontalBorder(world, minX, maxX, minZ)
            if (!hasBottom) drawHorizontalBorder(world, minX, maxX, maxZ)
        }
    }

    /**
     * Draws a vertical border using particles, covering the entire world height.
     * @param world The world where the particles should be spawned.
     * @param x The X-coordinate of the border.
     * @param startZ The starting Z-coordinate.
     * @param endZ The ending Z-coordinate.
     */
    private fun drawVerticalBorder(world: World, x: Int, startZ: Int, endZ: Int) {
        val minY = world.minHeight
        val maxY = world.maxHeight

        for (z in startZ..endZ step 1) {
            for (y in minY..maxY step 2) {
                world.spawnParticle(Particle.END_ROD, x.toDouble(), y.toDouble(), z.toDouble(), 2, 0.1, 0.1, 0.1, 0.01)
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
    private fun drawHorizontalBorder(world: World, startX: Int, endX: Int, z: Int) {
        val minY = world.minHeight
        val maxY = world.maxHeight

        for (x in startX..endX step 1) {
            for (y in minY..maxY step 2) {
                world.spawnParticle(Particle.END_ROD, x.toDouble(), y.toDouble(), z.toDouble(), 2, 0.1, 0.1, 0.1, 0.01)
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


