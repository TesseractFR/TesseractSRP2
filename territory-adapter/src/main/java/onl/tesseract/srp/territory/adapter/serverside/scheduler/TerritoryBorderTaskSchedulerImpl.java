package onl.tesseract.srp.territory.adapter.serverside.scheduler;

import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.territory.adapter.serverside.infrastructure.runnable.TerritoryBorderTask;
import onl.tesseract.srp.territory.domain.port.serverside.TerritoryBorderTaskScheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

@Component
public class TerritoryBorderTaskSchedulerImpl implements TerritoryBorderTaskScheduler {

    private static final long TICKS_PER_SECOND = 20L;

    private final Plugin plugin;
    private final Map<UUID, BukkitTask> activeTasks = new HashMap<>();

    public TerritoryBorderTaskSchedulerImpl(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void schedule(UUID player, List<ChunkCoord> chunkCoords) {
        org.bukkit.entity.Player bukkitPlayer = Bukkit.getPlayer(player);
        if (bukkitPlayer == null) {
            return;
        }
        TerritoryBorderTask task = new TerritoryBorderTask(bukkitPlayer, chunkCoords);
        BukkitTask bukkitTask = task.runTaskTimerAsynchronously(plugin, 0L, TICKS_PER_SECOND * 2);
        BukkitTask removed = activeTasks.remove(player);
        if (removed != null) {
            removed.cancel();
        }
        activeTasks.put(player, bukkitTask);
    }

    @Override
    public void cancel(UUID player) {
        BukkitTask removed = activeTasks.remove(player);
        if (removed != null) {
            removed.cancel();
        }
    }

    @Override
    public boolean isActive(UUID player) {
        return activeTasks.containsKey(player);
    }
}

