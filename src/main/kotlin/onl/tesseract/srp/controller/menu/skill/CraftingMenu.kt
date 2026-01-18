package onl.tesseract.srp.controller.menu.skill

import net.kyori.adventure.text.Component
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.srp.controller.menu.ItemAdderMenu
import onl.tesseract.srp.domain.item.CustomItemIds
import onl.tesseract.srp.domain.port.PlayerInventoryPort
import onl.tesseract.srp.domain.skill.Recipe
import onl.tesseract.srp.domain.skill.Skill
import onl.tesseract.srp.service.item.CustomItemService
import org.bukkit.Material
import org.bukkit.entity.Player
import kotlin.math.max
import kotlin.math.min

class CraftingMenu(val skill : Skill,
                   val customItemService: CustomItemService,
                   val playerInventoryPort: PlayerInventoryPort,
                   val activeRecipe: Recipe? = null ,previous : Menu? = null) : ItemAdderMenu(
    MenuSize.Six,"tesseract:test","testMenu",
    previous){

    private val FIRST_OFFSET = 1
    private val SECOND_OFFSET = 5

    private var quantityToCraft = 1

    override fun placeButtons(viewer: Player) {
        addRecipeButton(viewer)
        addActiveRecipe()

        addQuantityButtons(viewer)

        addBackButton()
        addCloseButton()
    }

    private fun addQuantityButtons(viewer: Player) {
        addButton(36, ItemBuilder(Material.PAPER,"min").build()){
            quantityToCraft = 1
            this.placeButtons(viewer)
        }
        addButton(37, ItemBuilder(Material.PAPER,"-$SECOND_OFFSET").build()){
            quantityToCraft = max(1,quantityToCraft-SECOND_OFFSET)
            this.placeButtons(viewer)
        }
        addButton(38, ItemBuilder(Material.PAPER,"-$FIRST_OFFSET").build()){
            quantityToCraft = max(1,quantityToCraft-FIRST_OFFSET)
            this.placeButtons(viewer)
        }
        addButton(39,ItemBuilder(Material.PAPER,"$quantityToCraft").build().asQuantity(quantityToCraft))
        addButton(40, ItemBuilder(Material.PAPER,"+$FIRST_OFFSET").build()){
            quantityToCraft = min(getMaxCraft(viewer),quantityToCraft+FIRST_OFFSET)
            this.placeButtons(viewer)
        }
        addButton(41, ItemBuilder(Material.PAPER,"+$SECOND_OFFSET").build()){
            quantityToCraft = min(getMaxCraft(viewer),quantityToCraft+SECOND_OFFSET)
            this.placeButtons(viewer)
        }
        addButton(42, ItemBuilder(Material.PAPER,"max").build()){
            quantityToCraft = getMaxCraft(viewer)
            this.placeButtons(viewer)
        }
    }

    private fun addActiveRecipe() {
        if(activeRecipe == null)return
        activeRecipe.components.forEach { (i, component) ->
            val item = component.item
            addButton(17+i,component.item.asQuantity(component.quantity*quantityToCraft))
        }
        addButton(26,activeRecipe.result.item.asQuantity(activeRecipe.result.quantity*quantityToCraft))
    }

    private fun addRecipeButton(viewer: Player) {
        val item = customItemService.getCustomItem(CustomItemIds.MENU_MENU_BUTTOM)
        item.editMeta { it.displayName(Component.text("Recettes")) }
        addButton(0, item) {
            RecipeMenu(skill, customItemService,playerInventoryPort, this).open(viewer);
        }
    }

    private fun getMaxCraft(viewer: Player): Int {
        var max = 0;
        activeRecipe?.components?.forEach { (i, component) ->
            max = max(max,playerInventoryPort.getItemNumber(viewer.uniqueId,component.item))
        }
        return max
    }


}