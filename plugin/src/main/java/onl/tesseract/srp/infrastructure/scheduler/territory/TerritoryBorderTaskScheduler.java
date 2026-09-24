package onl.tesseract.srp.infrastructure.scheduler.territory;

import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.territory.adapter.serverside.infrastructure.runnable.TerritoryBorderTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Schedules and manages territory border drawing tasks for players.
 */
@Component
public class TerritoryBorderTaskScheduler {
    private static final long TICKS_PER_SECOND = 20L;

    private final Plugin plugin;
    private final java.util.Map<UUID, BukkitTask> activeTasks = new java.util.HashMap<>();

    public TerritoryBorderTaskScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Schedules a border drawing task for the given player and chunks.
     */
    public void schedule(UUID player, java.util.Collection<ChunkCoord> chunks) {
        org.bukkit.entity.Player bukkitPlayer = Bukkit.getPlayer(player);
        if (bukkitPlayer == null) {
            return;
        }
        TerritoryBorderTask task = new TerritoryBorderTask(bukkitPlayer, chunks);
        BukkitTask bukkitTask = task.runTaskTimerAsynchronously(plugin, 0L, TICKS_PER_SECOND * 2);
        BukkitTask removed = activeTasks.remove(player);
        if (removed != null) {
            removed.cancel();
        }
        activeTasks.put(player, bukkitTask);
    }

    /**
     * Cancels the border drawing task for the given player.
     */
    public void cancel(UUID player) {
        BukkitTask removed = activeTasks.remove(player);
        if (removed != null) {
            removed.cancel();
        }
    }

    /**
     * Checks if a border drawing task is active for the given player.
     */
    public boolean isActive(UUID player) {
        return activeTasks.containsKey(player);
    }
}
