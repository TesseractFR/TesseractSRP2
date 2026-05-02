package onl.tesseract.srp.domain.skill.crafting

import onl.tesseract.srp.domain.item.Quality
import onl.tesseract.srp.domain.skill.recipe.Recipe

data class QueuedRecipe(
    val recipe: Recipe,
    val compoQuality: Quality,
    var quantity: Int,
)