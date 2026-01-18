package onl.tesseract.srp.controller.event.structure

import dev.lone.itemsadder.api.CustomFurniture
import onl.tesseract.srp.controller.menu.skill.CraftingMenu
import onl.tesseract.srp.domain.port.PlayerInventoryPort
import onl.tesseract.srp.service.item.CustomItemService
import onl.tesseract.srp.service.skill.SkillService
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class CustomStructureListener(val skillService: SkillService,
                              val customItemService: CustomItemService,
                              val playerInventoryPort: PlayerInventoryPort
    ) : Listener {

    private fun onClick(player: Player, furniture: CustomFurniture) : Boolean{
        val skill = skillService.getSkillFromStructureID(furniture.namespacedID)?:return false

        CraftingMenu(skill,customItemService,playerInventoryPort).open(player)

        return true
    }
    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent){
        val block = e.clickedBlock ?: return
        val furniture = CustomFurniture.byAlreadySpawned(block)?:return
        if (e.hand != EquipmentSlot.HAND || e.action != Action.RIGHT_CLICK_BLOCK) return
        e.isCancelled = onClick(e.player,furniture)

    }
}