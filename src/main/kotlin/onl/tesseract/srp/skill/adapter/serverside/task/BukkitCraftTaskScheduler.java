package onl.tesseract.srp.skill.adapter.serverside.task;

import onl.tesseract.srp.skill.domain.model.PlayerID;
import onl.tesseract.srp.skill.domain.model.crafting.CraftTask;
import onl.tesseract.srp.skill.domain.model.skill.SkillName;
import onl.tesseract.srp.skill.domain.port.serverside.CraftTaskScheduler;
import onl.tesseract.srp.skill.domain.port.userside.CraftingService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class BukkitCraftTaskScheduler implements CraftTaskScheduler {
    private final Plugin plugin;
    private final Map<PlayerID, Map<SkillName, CraftTasks>> activeTasks = new HashMap<>();

    public BukkitCraftTaskScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void schedule(PlayerID player, SkillName skill, CraftTask craftTask, CraftingService craftingService) {
        var runnable = new CraftRunnable(player, skill, craftTask, craftingService);
        var ticks = (craftTask.unitDuration().toMillis() / 50);
        var task = runnable.runTaskTimer(plugin, ticks, ticks);

        var tickRunnable = new CraftTickRunnable(craftTask);
        var timerTask = tickRunnable.runTaskTimer(plugin, 20L, 20L);
        cancel(player, skill);
        activeTasks.putIfAbsent(player, new HashMap<>());
        activeTasks.get(player).put(skill, new CraftTasks(task, timerTask));

    }

    @Override
    public void cancel(PlayerID playerId, SkillName name) {
        if (!activeTasks.containsKey(playerId)) {
            return;
        }
        Optional.ofNullable(activeTasks.get(playerId).remove(name))
                .ifPresent(task -> {
                    task.cancel();
                    Optional.ofNullable(Bukkit.getPlayer(playerId.value()))
                            .ifPresentOrElse(
                                    player -> player.sendMessage("§aFabrication de ${skillName} terminée !"),
                                    () -> System.err.println("Player not found")
                            );

                });

    }

    private record CraftTasks(
            BukkitTask craftTask,
            BukkitTask timerTask
    ) {
        public void cancel() {
            craftTask.cancel();
            timerTask.cancel();
        }
    }
}
