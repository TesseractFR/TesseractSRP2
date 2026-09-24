package onl.tesseract.srp.skill.adapter.serverside.task;

import onl.tesseract.srp.skill.domain.model.crafting.CraftTask;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;

public class CraftTickRunnable extends BukkitRunnable {
    private CraftTask craftTask;

    public CraftTickRunnable(CraftTask craftTask) {
        this.craftTask = craftTask;
    }

    @Override
    public void run() {
        craftTask = craftTask.tick(Duration.ofSeconds(1));
    }
}
