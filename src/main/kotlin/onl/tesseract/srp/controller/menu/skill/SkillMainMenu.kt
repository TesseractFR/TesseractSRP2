package onl.tesseract.srp.controller.menu.skill

import net.kyori.adventure.text.Component
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.srp.controller.menu.ItemAdderMenu
import onl.tesseract.srp.domain.item.CustomItemIds
import onl.tesseract.srp.domain.port.PlayerInventoryPort
import onl.tesseract.srp.domain.skill.Skill
import onl.tesseract.srp.service.item.CustomItemService
import org.bukkit.entity.Player

class SkillMainMenu(val skill : Skill,
                    val customItemService: CustomItemService,
                    val playerInventoryPort: PlayerInventoryPort,previous : Menu? = null): ItemAdderMenu(
    MenuSize.Six,"tesseract:recipe_advancement",skill.name, previous) {


    override fun placeButtons(viewer: Player) {
        addRecipeButton(viewer)

        super.placeButtons(viewer)
    }

    private fun addRecipeButton(viewer: Player) {
        val item = customItemService.getCustomItem(CustomItemIds.MENU_MENU_BUTTOM)
        item.editMeta { it.displayName(Component.text("Recettes")) }
        addButton(0, item) {
            RecipeMenu(skill, customItemService,playerInventoryPort, this).open(viewer);
        }
    }
}