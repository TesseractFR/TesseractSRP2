package onl.tesseract.srp.controller.event.structure

import dev.lone.itemsadder.api.CustomFurniture
import onl.tesseract.srp.customitem.adapter.userside.ItemGateway
import onl.tesseract.srp.domain.commun.ChunkCoord
import onl.tesseract.srp.domain.port.PlayerInventoryPort
import onl.tesseract.srp.service.item.CustomItemService
import onl.tesseract.srp.service.territory.guild.GuildService
import onl.tesseract.srp.skill.adapter.userside.controller.menu.SkillMainMenu
import onl.tesseract.srp.skill.domain.port.userside.CraftingService
import onl.tesseract.srp.skill.domain.port.userside.SkillService
import onl.tesseract.srp.skill.domain.port.userside.StationService
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class CustomStructureListener(
    val skillService: SkillService,
    val customItemService: CustomItemService,
    val itemGateway: ItemGateway,
    val craftingService: CraftingService,
    val stationService: StationService,
    val playerInventoryPort: PlayerInventoryPort,
    val guildService: GuildService
) : Listener {

    private fun onClick(player: Player, furniture: CustomFurniture) : Boolean{
        val skill = skillService.getSkillFromStructureID(furniture.namespacedID)?:return false
        val location = player.location
        val structure = stationService.getStationByChunkCoord(
            ChunkCoord(location.blockX,location.blockY,location.world.name),
            skill.name)


        SkillMainMenu(skill, itemGateway,craftingService,guildService,stationService,playerInventoryPort,structure,null).open(player)

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