package onl.tesseract.srp.controller.event.structure

import dev.lone.itemsadder.api.CustomFurniture
import onl.tesseract.srp.controller.menu.skill.CraftingMenu
import onl.tesseract.srp.service.item.CustomItemService
import onl.tesseract.srp.service.skill.SkillService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class SkillStructureListener(val skillService: SkillService,val customItemService: CustomItemService) :  CustomStructureListener() {
    override fun onClick(player: Player, furniture: CustomFurniture) : Boolean{
        val skill = skillService.getSkillFromStructureID(furniture.namespacedID)?:return false

        CraftingMenu(skill,customItemService).open(player)

        return true
    }
}