package onl.tesseract.srp.skill.domain.model.crafting;

import onl.tesseract.srp.skill.domain.model.skill.SkillName;

import java.util.Map;
import java.util.Queue;

public record PerSkillQueue(
        Map<SkillName, Queue<QueuedRecipe>> queuedRecipes
) {

}
