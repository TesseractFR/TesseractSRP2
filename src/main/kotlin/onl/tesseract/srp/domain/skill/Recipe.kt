package onl.tesseract.srp.domain.skill

import org.bukkit.inventory.ItemStack

data class Recipe(
    val components : Map<Int, RecipeComponent>,
    val result: RecipeComponent
)

data class RecipeComponent(
    val item: ItemStack,
    val quantity: Int
)