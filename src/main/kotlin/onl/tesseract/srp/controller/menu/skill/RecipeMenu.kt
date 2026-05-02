package onl.tesseract.srp.controller.menu.skill

import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.srp.controller.menu.ItemAdderBiMenu
import onl.tesseract.srp.domain.item.CustomItemIds
import onl.tesseract.srp.domain.port.PlayerInventoryPort
import onl.tesseract.srp.domain.skill.recipe.Recipe
import onl.tesseract.srp.domain.skill.Skill
import onl.tesseract.srp.service.item.CustomItemService
import onl.tesseract.srp.service.skill.RecipeService
import onl.tesseract.srp.service.skill.SkillService
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.collections.iterator

class RecipeMenu(
    val skill: Skill,
    val customItemService: CustomItemService,
    val playerInventoryPort: PlayerInventoryPort,
    val recipeService: RecipeService,
    val skillService: SkillService,
    previous: Menu? = null,
) :
        ItemAdderBiMenu(MenuSize.Six,"tesseract:recipe_book","Recettes "+skill.name, previous,100){

    var tier = 1
    var startingRecipe = 0

    override fun placeButtons(viewer: Player) {
        addButton(0,customItemService.getCustomItem(CustomItemIds.MENU_BACK_ARROW_BUTTON)){
            if(previous==null){
                this.close()
                return@addButton
            }
            previous?.open(viewer)
        }
        placeRecipes(viewer)
        //addButton(51,customItemService.getCustomItem(CustomItemIds.MENU_RIGHT_ARROW_BUTTOM))
        //addButton(45,customItemService.getCustomItem(CustomItemIds.MENU_LEFT_ARROW_BUTTOM))
        addBottomCloseButton()
    }
    private fun placeRecipes(viewer: Player){
        val recipes = skill.recipe[tier]?.recipes
        if(recipes == null){
            tier = 1
            placeRecipes(viewer)
            return
        }
        for( i in 1..7){
            val recipe = recipes[i+startingRecipe]
            if(recipe == null){
                clearLigne(i)
                return
            }
            placeRecipe(i,recipe,viewer)
        }

        addButton(8,customItemService
                .getCustomItem(CustomItemIds.MENU_UP_ARROW_BUTTON)
                .asQuantity(if(startingRecipe>0)1 else 0)){
            startingRecipe--
            placeRecipes(viewer)
        }
        addBottomButton(35,customItemService
                .getCustomItem(CustomItemIds.MENU_DOWN_ARROW_BUTTON)
                .asQuantity(if(recipes.size > startingRecipe+7)1 else 0)){
            startingRecipe++
            placeRecipes(viewer)
        }

    }

    private fun clearLigne(lign: Int) {
        for(i in 0..8){
            addButton(lign*9+i, ItemStack(Material.STONE).asQuantity(0)){}
        }
    }

    private fun placeRecipe(ligne: Int, recipe: Recipe, viewer: Player){
        val comps = recipe.components
        for (com in comps){
            val col = com.key
            val item = customItemService.toItemstack(com.value.item)
            item.amount = com.value.quantity
            addButton(9*(ligne)+(col-1),item)


        }
        val item = customItemService.toItemstack(recipe.result.item)
        item.amount = recipe.result.quantity
        addButton(9*(ligne)+(8),item){
            CraftingMenu(skill,customItemService, recipeService, skillService, playerInventoryPort,recipe,this).open(viewer)
        }

    }
}