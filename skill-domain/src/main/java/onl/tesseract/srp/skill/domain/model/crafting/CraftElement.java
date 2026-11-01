package onl.tesseract.srp.skill.domain.model.crafting;

import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.skill.domain.model.recipe.Material;

public record CraftElement(
        Material material,
        int quantity,
        Quality quality
) {
}
