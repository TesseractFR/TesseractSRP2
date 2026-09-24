package onl.tesseract.srp.skill.domain.model.crafting;

import lombok.Data;
import onl.tesseract.srp.skill.domain.model.Quality;
import onl.tesseract.srp.skill.domain.model.bonus.CraftingBonus;
import onl.tesseract.srp.skill.domain.model.recipe.Recipe;

@Data
public class QueuedRecipe {
    private final Recipe recipe;
    private final Quality compoQuality;
    private final CraftingBonus bonus;
    private Integer quantity;

    public QueuedRecipe(Recipe recipe, Quality compoQuality, CraftingBonus bonus, Integer quantity) {
        this.recipe = recipe;
        this.compoQuality = compoQuality;
        this.bonus = bonus;
        this.quantity = quantity;
    }
}
