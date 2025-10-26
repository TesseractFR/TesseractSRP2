package onl.tesseract.srp.controller.menu.skill

import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.srp.controller.menu.ItemAdderMenu
import onl.tesseract.srp.domain.skill.Skill
import onl.tesseract.srp.service.item.CustomItemService
import onl.tesseract.srp.util.CUSTOM_TEXTURE_NAMESPACE_MENU_DOWN_ARROW_BUTTOM
import onl.tesseract.srp.util.CUSTOM_TEXTURE_NAMESPACE_MENU_LEFT_ARROW_BUTTOM
import onl.tesseract.srp.util.CUSTOM_TEXTURE_NAMESPACE_MENU_RETURN_BUTTOM
import onl.tesseract.srp.util.CUSTOM_TEXTURE_NAMESPACE_MENU_RIGHT_ARROW_BUTTOM
import onl.tesseract.srp.util.CUSTOM_TEXTURE_NAMESPACE_MENU_UP_ARROW_BUTTOM
import org.bukkit.entity.Player

class RecipeMenu(val skill : Skill,val customItemService: CustomItemService,previous: Menu? = null) : ItemAdderMenu(MenuSize.Six,"tesseract:skill_recipe","Recettes "+skill.name,
    previous){

    val tier = 1


    override fun placeButtons(viewer: Player) {
        addButton(0,customItemService.getCustomItem(CUSTOM_TEXTURE_NAMESPACE_MENU_RETURN_BUTTOM)){
            if(previous==null){
                this.close()
                return@addButton
            }
            previous?.open(viewer)
        }
        addButton(8,customItemService.getCustomItem(CUSTOM_TEXTURE_NAMESPACE_MENU_UP_ARROW_BUTTOM))
        addButton(53,customItemService.getCustomItem(CUSTOM_TEXTURE_NAMESPACE_MENU_DOWN_ARROW_BUTTOM))
        addButton(51,customItemService.getCustomItem(CUSTOM_TEXTURE_NAMESPACE_MENU_RIGHT_ARROW_BUTTOM))
        addButton(45,customItemService.getCustomItem(CUSTOM_TEXTURE_NAMESPACE_MENU_LEFT_ARROW_BUTTOM))

        for (rec in this.skill.recipe[tier]?.recipes?.entries!!){
            val lign = rec.key
            val comps = rec.value.components
            for (com in comps){
                val col = com.key
                val item = com.value.item
                item.amount = com.value.quantity
                addButton(9*(lign)+(col-1),item)
            }
            val item = rec.value.result.item
            item.amount = rec.value.result.quantity
            addButton(9*(lign)+(8),item)
        }
    }
}