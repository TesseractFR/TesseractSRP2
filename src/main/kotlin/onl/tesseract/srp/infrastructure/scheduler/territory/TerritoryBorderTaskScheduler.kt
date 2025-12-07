package onl.tesseract.srp.infrastructure.scheduler.territory

import onl.tesseract.srp.domain.commun.ChunkCoord
import onl.tesseract.srp.infrastructure.runnable.territory.TerritoryBorderTask
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import org.springframework.stereotype.Component
import java.util.UUID

private const val TICKS_PER_SECOND = 20L

@Component
class TerritoryBorderTaskScheduler (
    private val plugin: Plugin
){

    private val activeTasks = mutableMapOf<UUID, BukkitTask>()


    fun schedule(player: UUID, chunks: Collection<ChunkCoord>) {
        val task = TerritoryBorderTask(Bukkit.getPlayer(player)?:return,chunks)
                .runTaskTimerAsynchronously(plugin, 0L, TICKS_PER_SECOND*2)
        activeTasks.remove(player)?.cancel()
        activeTasks[player] = task
    }

    fun cancel(player: UUID) {
        activeTasks.remove(player)?.cancel()
    }

    fun isActive(player: UUID) : Boolean {
        return activeTasks.containsKey(player)
    }
}