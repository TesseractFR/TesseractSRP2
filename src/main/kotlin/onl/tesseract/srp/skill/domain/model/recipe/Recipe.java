package onl.tesseract.srp.skill.domain.model.recipe;

import java.time.Duration;
import java.util.Map;

public record Recipe(
        RecipeName name,
        Slot slot,
        Map<IngredientSlot, RecipeComponent> components,
        RecipeComponent result,
        Tier tier,
        Duration duration
) {}