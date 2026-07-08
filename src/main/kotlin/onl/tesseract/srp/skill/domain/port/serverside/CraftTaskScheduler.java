package onl.tesseract.srp.skill.domain.port.serverside;

import onl.tesseract.srp.skill.domain.model.PlayerID;
import onl.tesseract.srp.skill.domain.model.crafting.CraftTask;
import onl.tesseract.srp.skill.domain.model.skill.SkillName;
import onl.tesseract.srp.skill.domain.port.userside.CraftingService;

import java.util.UUID;

public interface CraftTaskScheduler {
    void schedule(PlayerID player, SkillName skill, CraftTask craftTask, CraftingService craftingService);

    void cancel(PlayerID playerId, SkillName name);
}
