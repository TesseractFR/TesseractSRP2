package onl.tesseract.srp.skill.adapter.serverside.task;

import onl.tesseract.srp.skill.domain.model.PlayerID;
import onl.tesseract.srp.skill.domain.model.crafting.CraftTask;
import onl.tesseract.srp.skill.domain.model.skill.SkillName;
import onl.tesseract.srp.skill.domain.port.userside.CraftingService;
import org.bukkit.scheduler.BukkitRunnable;

public class CraftRunnable extends BukkitRunnable {

    private final PlayerID playerUUID;
    private final SkillName skill;
    private final CraftTask craftTask;
    private final CraftingService craftingService;

    public CraftRunnable(PlayerID playerUUID, SkillName skill, CraftTask craftTask, CraftingService craftingService) {
        this.playerUUID = playerUUID;
        this.skill = skill;
        this.craftTask = craftTask;
        this.craftingService = craftingService;
    }

    @Override
    public void run() {
        craftingService.processCraftStep(playerUUID, skill, craftTask);
    }
}
