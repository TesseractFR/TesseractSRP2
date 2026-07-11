package onl.tesseract.srp.skill.domain.model.skill;

import onl.tesseract.srp.skill.domain.model.recipe.Recipe;

import java.util.Map;

public record SkillTier(
        Map<Integer, Recipe> recipes,
        Map<String, Recipe> recipesByName
) {
}
