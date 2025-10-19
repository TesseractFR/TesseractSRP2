package onl.tesseract.srp.controller.menu.craftmanship

import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.srp.controller.menu.ItemAdderMenu
import onl.tesseract.srp.domain.craftingjob.Recipe
import onl.tesseract.srp.domain.craftingjob.Skill
import org.bukkit.entity.Player

class CraftingMenu(val skill : Skill) : ItemAdderMenu(
    MenuSize.Six,"tesseract:test","testMenu",
    null){

    override fun placeButtons(viewer: Player) {
        for (rec in this.skill.recipe[1]?.recipes?.entries!!){
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