package onl.tesseract.srp.skill.domain.model.skill;

import onl.tesseract.srp.skill.domain.model.recipe.Tier;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record Skill(
        SkillName name,
        SkillStructureName structure,
        @NotNull
        Map<@NotNull Tier,@NotNull SkillTier> tiers
) {
}
