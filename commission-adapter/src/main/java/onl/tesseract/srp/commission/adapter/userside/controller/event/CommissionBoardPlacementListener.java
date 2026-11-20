package onl.tesseract.srp.commission.adapter.userside.controller.event;

import onl.tesseract.srp.commission.adapter.furniture.CommissionBoardFurniture;
import onl.tesseract.srp.commission.adapter.userside.controller.message.CommissionMessageFormatter;
import onl.tesseract.srp.commission.domain.model.board.BoardFacing;
import onl.tesseract.srp.commission.domain.model.board.BoardSpot;
import onl.tesseract.srp.commission.domain.model.exception.CommissionException;
import onl.tesseract.srp.commission.domain.port.userside.CommissionBoardPlacementService;
import onl.tesseract.srp.common.domain.model.PlayerId;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Optional;

/** Pendant la pose du tableau d'une guilde : un clic droit au sol le pose, s'accroupir annule. */
@org.springframework.stereotype.Component
public class CommissionBoardPlacementListener implements Listener {

    private final CommissionBoardPlacementService commissionBoardPlacementService;
    private final CommissionMessageFormatter commissionMessageFormatter;

    public CommissionBoardPlacementListener(CommissionBoardPlacementService commissionBoardPlacementService,
                                            CommissionMessageFormatter commissionMessageFormatter) {
        this.commissionBoardPlacementService = commissionBoardPlacementService;
        this.commissionMessageFormatter = commissionMessageFormatter;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        PlayerId playerId = new PlayerId(event.getPlayer().getUniqueId());
        if (event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK
                || !commissionBoardPlacementService.isPlacingBoard(playerId)) {
            return;
        }
        Optional.ofNullable(event.getClickedBlock()).ifPresent(clickedBlock -> placeBoardOn(event, playerId, clickedBlock));
    }

    private void placeBoardOn(PlayerInteractEvent event, PlayerId playerId, Block clickedBlock) {
        Player player = event.getPlayer();
        event.setCancelled(true);
        if (event.getBlockFace() != BlockFace.UP) {
            player.sendMessage(commissionMessageFormatter.formatBoardPlacementNotOnTopOfBlock());
            return;
        }
        BoardFacing facing = BoardFacing.valueOf(player.getFacing().getOppositeFace().name());
        BoardSpot boardSpot = new BoardSpot(
                CommissionBoardFurniture.toBlockCoordinate(clickedBlock.getRelative(BlockFace.UP).getLocation()), facing);
        try {
            commissionBoardPlacementService.placeGuildBoard(playerId, boardSpot);
            player.sendMessage(commissionMessageFormatter.formatBoardPlaced());
        } catch (CommissionException refusal) {
            player.sendMessage(commissionMessageFormatter.formatRefusal(refusal));
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking() && commissionBoardPlacementService.cancelBoardPlacement(new PlayerId(event.getPlayer().getUniqueId()))) {
            event.getPlayer().sendMessage(commissionMessageFormatter.formatBoardPlacementCancelled());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        commissionBoardPlacementService.cancelBoardPlacement(new PlayerId(event.getPlayer().getUniqueId()));
    }
}
