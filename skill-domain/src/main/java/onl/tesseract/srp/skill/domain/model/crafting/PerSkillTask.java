package onl.tesseract.srp.skill.domain.model.crafting;

import onl.tesseract.srp.skill.domain.model.skill.SkillName;

import java.util.Map;

public record PerSkillTask(
        Map<SkillName, CraftTask> tasks
) {
}
