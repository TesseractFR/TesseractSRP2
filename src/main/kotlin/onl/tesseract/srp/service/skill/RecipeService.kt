package onl.tesseract.srp.service.skill

import onl.tesseract.srp.domain.skill.recipe.Recipe
import onl.tesseract.srp.service.item.CustomItemService
import org.springframework.stereotype.Component
import kotlin.math.min

@Component
class RecipeService(
    val customItemService: CustomItemService
) {
    fun getMaxStackSize(recipe: Recipe): Int {
        var maxCompo = 64
        for (comp in recipe.components.values){
            maxCompo = min(maxCompo,customItemService.toItemstack(comp.item).maxStackSize /comp.quantity)
        }
        return min(customItemService.toItemstack(recipe.result.item).maxStackSize /recipe.result.quantity,maxCompo)
    }
}