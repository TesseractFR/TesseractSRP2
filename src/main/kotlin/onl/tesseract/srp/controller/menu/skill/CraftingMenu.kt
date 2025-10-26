package onl.tesseract.srp.controller.menu.skill

import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.srp.controller.menu.ItemAdderMenu
import onl.tesseract.srp.domain.skill.Skill
import onl.tesseract.srp.service.item.CustomItemService
import onl.tesseract.srp.util.CUSTOM_TEXTURE_NAMESPACE_MENU_MENU_BUTTOM
import org.bukkit.entity.Player

class CraftingMenu(val skill : Skill,val customItemService: CustomItemService) : ItemAdderMenu(
    MenuSize.Six,"tesseract:test","testMenu",
    null){


    override fun placeButtons(viewer: Player) {
        addButton(0,customItemService.getCustomItem(CUSTOM_TEXTURE_NAMESPACE_MENU_MENU_BUTTOM)){
            RecipeMenu(skill,customItemService,this).open(viewer);
        }
    }

}