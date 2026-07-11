package onl.tesseract.srp.domain.skill.crafting

import onl.tesseract.srp.customitem.domain.model.Quality
import onl.tesseract.srp.skill.domain.model.recipe.Recipe

data class QueuedRecipe(
    val recipe: Recipe,
    val compoQuality: Quality,
    var quantity: Int,
)