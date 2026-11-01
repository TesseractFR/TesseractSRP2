package onl.tesseract.srp.skill.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.skill.domain.model.crafting.CraftTask;
import onl.tesseract.srp.skill.domain.model.skill.SkillName;
import onl.tesseract.srp.skill.domain.port.userside.CraftingService;

public interface CraftTaskScheduler {
    void schedule(PlayerId player, SkillName skill, CraftTask craftTask, CraftingService craftingService);

    void cancel(PlayerId playerId, SkillName name);
}
