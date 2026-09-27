package onl.tesseract.srp.skill.adapter.userside.controller.event;

import dev.lone.itemsadder.api.CustomFurniture;
import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;
import onl.tesseract.srp.skill.adapter.userside.controller.menu.SkillMainMenu;
import onl.tesseract.srp.skill.domain.port.userside.CraftingService;
import onl.tesseract.srp.skill.domain.port.userside.SkillService;
import onl.tesseract.srp.skill.domain.port.userside.StationService;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.springframework.stereotype.Component;

@Component
public class CustomStructureListener implements Listener {

    private final SkillService skillService;
    private final CustomItemService customItemService;
    private final CraftingService craftingService;
    private final StationService stationService;
    private final GuildService guildService;

    public CustomStructureListener(
            SkillService skillService,
            CustomItemService customItemService,
            CraftingService craftingService,
            StationService stationService,
            GuildService guildService
    ) {
        this.skillService = skillService;
        this.customItemService = customItemService;
        this.craftingService = craftingService;
        this.stationService = stationService;
        this.guildService = guildService;
    }

    private boolean onClick(Player player, CustomFurniture furniture) {
        var skill = skillService.getSkillFromStructureID(furniture.getNamespacedID());
        if (skill == null) {
            return false;
        }

        var location = player.getLocation();
        var structure = stationService.getStationByChunkCoord(
                new ChunkCoord(location.getBlockX(), location.getBlockY(), location.getWorld().getName()),
                skill.name()
        );

        new SkillMainMenu(
                skill,
                structure,
                craftingService,
                customItemService,
                guildService,
                stationService,
                null
        ).open(player);

        return true;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        var block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        var furniture = CustomFurniture.byAlreadySpawned(block);
        if (furniture == null) {
            return;
        }

        event.setCancelled(onClick(event.getPlayer(), furniture));
    }
}
