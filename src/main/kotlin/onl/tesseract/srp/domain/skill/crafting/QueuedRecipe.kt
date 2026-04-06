package onl.tesseract.srp.domain.skill.crafting

import onl.tesseract.srp.domain.skill.recipe.Recipe

data class QueuedRecipe(
    val retrieved: Int,
    val crafted: Int,
    val total: Int,
    val recipe: Recipe
)