package onl.tesseract.srp.controller.menu.skill

import net.kyori.adventure.text.Component
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.srp.controller.menu.ItemAdderMenu
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
import kotlin.math.max
import kotlin.math.min

private const val COMPONENTS_OFFSET = -1
private const val RESULT_INDEX = 8

private const val INFO_BUTTON_INDEX = 18

private const val QUANTITY_PLUS_FIRST_INDEX = 21
private const val QUANTITY_PLUS_SECOND_INDEX = 22
private const val QUANTITY_MAX_INDEX = 23

private const val QUANTITY_DISPLAY_INDEX = 31
private const val LAUNCH_BUTTON_INDEX = 34

private const val RECIPE_BOOK_BUTTON_INDEX = 36
private const val QUANTITY_MINUS_FIRST_INDEX = 39
private const val QUANTITY_MINUS_SECOND_INDEX = 40
private const val QUANTITY_MIN_INDEX = 41

class CraftingMenu(val skill : Skill,
                   val customItemService: CustomItemService,
                   val recipeService: RecipeService,
                   val skillService: SkillService,
                   val playerInventoryPort: PlayerInventoryPort,
                   val activeRecipe: Recipe ,previous : Menu? = null) : ItemAdderMenu(
    MenuSize.Six,"tesseract:recipe_launch","testMenu",
    previous){

    private val FIRST_OFFSET = 1
    private val SECOND_OFFSET = 5

    private var quantityToCraft = 1

    override fun placeButtons(viewer: Player) {
        addRecipeButton(viewer)
        addActiveRecipe()
        addInfoButton()
        addLaunchButton()
        addQuantityButtons(viewer)
    }

    private fun addQuantityButtons(viewer: Player) {
        addButton(QUANTITY_MIN_INDEX, customItemService.getCustomItem(CustomItemIds.MENU_MIN_BUTTON).also {
            it.editMeta { m -> m.displayName(Component.text("min")) }
        }) {
            quantityToCraft = 1
            this.placeButtons(viewer)
        }
        addButton(QUANTITY_MINUS_SECOND_INDEX, customItemService.getCustomItem(CustomItemIds.MENU_MINUS_5_BUTTON).also {
            it.editMeta { m -> m.displayName(Component.text("-$SECOND_OFFSET")) }
        }) {
            quantityToCraft = max(1, quantityToCraft - SECOND_OFFSET)
            this.placeButtons(viewer)
        }
        addButton(QUANTITY_MINUS_FIRST_INDEX, customItemService.getCustomItem(CustomItemIds.MENU_MINUS_1_BUTTON).also {
            it.editMeta { m -> m.displayName(Component.text("-$FIRST_OFFSET")) }
        }) {
            quantityToCraft = max(1, quantityToCraft - FIRST_OFFSET)
            this.placeButtons(viewer)
        }
        addButton(QUANTITY_DISPLAY_INDEX, ItemBuilder(Material.PAPER, "$quantityToCraft").build().asQuantity(quantityToCraft))
        addButton(QUANTITY_PLUS_FIRST_INDEX, customItemService.getCustomItem(CustomItemIds.MENU_PLUS_1_BUTTON).also {
            it.editMeta { m -> m.displayName(Component.text("+$FIRST_OFFSET")) }
        }) {
            quantityToCraft = min(getMaxCraft(viewer), quantityToCraft + FIRST_OFFSET)
            this.placeButtons(viewer)
        }
        addButton(QUANTITY_PLUS_SECOND_INDEX, customItemService.getCustomItem(CustomItemIds.MENU_PLUS_5_BUTTON).also {
            it.editMeta { m -> m.displayName(Component.text("+$SECOND_OFFSET")) }
        }) {
            quantityToCraft = min(getMaxCraft(viewer), quantityToCraft + SECOND_OFFSET)
            this.placeButtons(viewer)
        }
        addButton(QUANTITY_MAX_INDEX, customItemService.getCustomItem(CustomItemIds.MENU_MAX_BUTTON).also {
            it.editMeta { m -> m.displayName(Component.text("max")) }
        }) {
            quantityToCraft = getMaxCraft(viewer)
            this.placeButtons(viewer)
        }
    }

    private fun addActiveRecipe() {
        activeRecipe.components.forEach { (i, component) ->
            addButton(COMPONENTS_OFFSET+i,
                customItemService.toItemstack(component.item)
                        .asQuantity(component.quantity*quantityToCraft))
        }
        addButton(RESULT_INDEX, customItemService.toItemstack(activeRecipe.result.item)
                .asQuantity(activeRecipe.result.quantity*quantityToCraft))
    }

    private fun addInfoButton() {
        val item = customItemService.getCustomItem(CustomItemIds.MENU_INFORMATION_BUTTON)
        item.editMeta { it.displayName(Component.text("Informations")) }
        addButton(INFO_BUTTON_INDEX, item) {}
    }

    private fun addRecipeButton(viewer: Player) {
        val item = customItemService.getCustomItem(CustomItemIds.MENU_RECIPE_BOOK_BUTTON)
        item.editMeta { it.displayName(Component.text("Recettes")) }
        addButton(RECIPE_BOOK_BUTTON_INDEX, item) { event ->
            RecipeMenu(skill, customItemService, playerInventoryPort, recipeService, skillService, this).open(event.whoClicked as Player);
        }
    }

    private fun addLaunchButton() {
        val item = ItemStack(Material.ANVIL)
        item.editMeta { it.displayName(Component.text("Lancer la fabrication")) }
        addButton(LAUNCH_BUTTON_INDEX, item) { event ->
            val player = event.whoClicked as Player
            skillService.startCraft(player.uniqueId, skill, activeRecipe, quantityToCraft)
            this.close()
        }
    }

    private fun getMaxCraft(viewer: Player): Int {
        var max = 0;
        activeRecipe.components.forEach { (i, component) ->
            max = max(max,playerInventoryPort.getItemNumber(viewer.uniqueId,
                customItemService.toItemstack(component.item)))
        }
        return min(max,recipeService.getMaxStackSize(activeRecipe))
    }


}