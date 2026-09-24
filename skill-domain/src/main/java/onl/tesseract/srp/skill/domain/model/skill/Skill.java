package onl.tesseract.srp.skill.domain.model.skill;

import lombok.NonNull;
import onl.tesseract.srp.skill.domain.model.recipe.Tier;

import java.util.Map;

public record Skill(
        SkillName name,
        SkillStructureName structure,
        @NonNull
        Map<@NonNull Tier,@NonNull SkillTier> tiers
) {
}
