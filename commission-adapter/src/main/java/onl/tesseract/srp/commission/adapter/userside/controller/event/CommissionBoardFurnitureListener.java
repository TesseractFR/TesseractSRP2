package onl.tesseract.srp.commission.adapter.userside.controller.event;

import dev.lone.itemsadder.api.Events.FurnitureBreakEvent;
import dev.lone.itemsadder.api.Events.FurnitureInteractEvent;
import onl.tesseract.srp.commission.adapter.furniture.CommissionBoardFurniture;
import onl.tesseract.srp.commission.adapter.userside.controller.menu.CommissionBoardMenu;
import onl.tesseract.srp.commission.adapter.userside.controller.message.CommissionMessageFormatter;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.commission.domain.port.userside.CommissionBoardManagementService;
import onl.tesseract.srp.commission.domain.port.userside.CommissionBoardPlacementService;
import onl.tesseract.srp.commission.domain.port.userside.CommissionBoardService;
import onl.tesseract.srp.commission.domain.port.userside.CommissionDeliveryService;
import onl.tesseract.srp.common.adapter.mapper.ChunkCoordMapper;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;
import onl.tesseract.srp.territory.domain.model.guild.Guild;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Optional;

/**
 * Réagit au tableau posé : un clic ouvre le tableau de la guilde du territoire, ou celui du spawn
 * hors des guildes. Un tableau de guilde ne peut pas être cassé.
 */
@org.springframework.stereotype.Component
public class CommissionBoardFurnitureListener implements Listener {

    private final CommissionBoardService commissionBoardService;
    private final CommissionBoardManagementService commissionBoardManagementService;
    private final CommissionBoardPlacementService commissionBoardPlacementService;
    private final CommissionDeliveryService commissionDeliveryService;
    private final GuildService guildService;
    private final CustomItemService customItemService;
    private final CommissionMessageFormatter commissionMessageFormatter;

    public CommissionBoardFurnitureListener(CommissionBoardService commissionBoardService,
                                            CommissionBoardManagementService commissionBoardManagementService,
                                            CommissionBoardPlacementService commissionBoardPlacementService,
                                            CommissionDeliveryService commissionDeliveryService, GuildService guildService,
                                            CustomItemService customItemService, CommissionMessageFormatter commissionMessageFormatter) {
        this.commissionBoardService = commissionBoardService;
        this.commissionBoardManagementService = commissionBoardManagementService;
        this.commissionBoardPlacementService = commissionBoardPlacementService;
        this.commissionDeliveryService = commissionDeliveryService;
        this.guildService = guildService;
        this.customItemService = customItemService;
        this.commissionMessageFormatter = commissionMessageFormatter;
    }

    @EventHandler
    public void onFurnitureInteract(FurnitureInteractEvent event) {
        PlayerId player = new PlayerId(event.getPlayer().getUniqueId());
        if (!CommissionBoardFurniture.BOARD_ITEM_ID.equals(event.getNamespacedID()) || commissionBoardPlacementService.isPlacingBoard(player)) {
            return;
        }
        event.setCancelled(true);
        CommissionBoard commissionBoard = findGuildOwningLocation(event.getBukkitEntity().getLocation())
                .map(commissionBoardService::getGuildBoard)
                .orElseGet(commissionBoardService::getSpawnBoard);
        new CommissionBoardMenu(commissionBoard, commissionBoardService, commissionBoardManagementService, commissionDeliveryService,
                customItemService, commissionMessageFormatter).open(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onFurnitureBreak(FurnitureBreakEvent event) {
        boolean isOnGuildTerritory = findGuildOwningLocation(event.getBukkitEntity().getLocation()).isPresent();
        if (isOnGuildTerritory && CommissionBoardFurniture.BOARD_ITEM_ID.equals(event.getNamespacedID())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(commissionMessageFormatter.formatBoardCannotBeBroken());
        }
    }

    private Optional<GuildId> findGuildOwningLocation(Location location) {
        return Optional.ofNullable(guildService.getByChunk(ChunkCoordMapper.toChunkCoord(location)))
                .map(Guild::getId)
                .map(GuildId::new);
    }
}
