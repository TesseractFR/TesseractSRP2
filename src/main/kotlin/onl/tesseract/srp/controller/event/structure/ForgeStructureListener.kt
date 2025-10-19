package onl.tesseract.srp.controller.event.structure

import dev.lone.itemsadder.api.CustomFurniture
import net.kyori.adventure.text.Component
import onl.tesseract.srp.controller.menu.craftmanship.CraftingMenu
import onl.tesseract.srp.service.skill.SkillService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class SkillStructureListener(val skillService: SkillService) :  CustomStructureListener() {
    override fun onClick(player: Player, furniture: CustomFurniture) : Boolean{
        val skill = skillService.getSkillFromStructureID(furniture.namespacedID)?:return false

        CraftingMenu(skill).open(player)

        return true
    }
}