package onl.tesseract.srp.commission.adapter.userside.controller.event;

import dev.lone.itemsadder.api.Events.FurnitureInteractEvent;
import onl.tesseract.srp.commission.adapter.userside.controller.menu.CommissionBoardMenu;
import onl.tesseract.srp.commission.adapter.userside.controller.message.CommissionMessageFormatter;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.commission.domain.port.userside.CommissionBoardAccessService;
import onl.tesseract.srp.commission.domain.port.userside.CommissionBoardManagementService;
import onl.tesseract.srp.commission.domain.port.userside.CommissionBoardService;
import onl.tesseract.srp.commission.domain.port.userside.CommissionDeliveryService;
import onl.tesseract.srp.common.adapter.mapper.ChunkCoordMapper;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;
import onl.tesseract.srp.territory.domain.model.guild.Guild;
import onl.tesseract.srp.territory.domain.port.userside.guild.GuildService;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Réagit au tableau posé : un clic ouvre le tableau de la guilde du territoire, ou celui du spawn
 * hors des guildes. La pose et la récupération du tableau sont gérées par le moteur des structures.
 */
@Component
public class CommissionBoardClickListener implements Listener {
    private static final String BOARD_ITEM_ID = "tesseract:commission_board";

    private final CommissionBoardService commissionBoardService;
    private final CommissionBoardAccessService commissionBoardAccessService;
    private final CommissionBoardManagementService commissionBoardManagementService;
    private final CommissionDeliveryService commissionDeliveryService;
    private final GuildService guildService;
    private final CustomItemService customItemService;
    private final CommissionMessageFormatter commissionMessageFormatter;

    public CommissionBoardClickListener(CommissionBoardService commissionBoardService,
                                        CommissionBoardAccessService commissionBoardAccessService,
                                        CommissionBoardManagementService commissionBoardManagementService,
                                        CommissionDeliveryService commissionDeliveryService, GuildService guildService,
                                        CustomItemService customItemService, CommissionMessageFormatter commissionMessageFormatter) {
        this.commissionBoardService = commissionBoardService;
        this.commissionBoardAccessService = commissionBoardAccessService;
        this.commissionBoardManagementService = commissionBoardManagementService;
        this.commissionDeliveryService = commissionDeliveryService;
        this.guildService = guildService;
        this.customItemService = customItemService;
        this.commissionMessageFormatter = commissionMessageFormatter;
    }

    @EventHandler
    public void onFurnitureInteract(FurnitureInteractEvent event) {
        if (!BOARD_ITEM_ID.equals(event.getNamespacedID())) {
            return;
        }
        event.setCancelled(true);
        CommissionBoard commissionBoard = findGuildOwningLocation(event.getBukkitEntity().getLocation())
                .map(commissionBoardService::openGuildBoard)
                .orElseGet(commissionBoardService::openSpawnBoard);
        new CommissionBoardMenu(commissionBoard, commissionBoardService, commissionBoardAccessService, commissionBoardManagementService,
                commissionDeliveryService, customItemService, commissionMessageFormatter).open(event.getPlayer());
    }

    private Optional<GuildId> findGuildOwningLocation(Location location) {
        return Optional.ofNullable(guildService.getByChunk(ChunkCoordMapper.toChunkCoord(location)))
                .map(Guild::getId)
                .map(GuildId::new);
    }
}
