package onl.tesseract.srp.contract.adapter.userside.controller.event;

import onl.tesseract.srp.contract.adapter.utils.ContractScrollItem;
import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.port.userside.ContractService;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.springframework.stereotype.Component;

import java.util.OptionalLong;

@Component
public class ContractScrollListener implements Listener {

    private final ContractService contractService;

    public ContractScrollListener(ContractService contractService) {
        this.contractService = contractService;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (ContractScrollItem.readContractId(event.getItemInHand()).isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        for (ItemStack ingredient : event.getInventory().getMatrix()) {
            if (ContractScrollItem.readContractId(ingredient).isPresent()) {
                event.getInventory().setResult(null);
                return;
            }
        }
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        Player player = event.getPlayer();
        OptionalLong id = ContractScrollItem.readContractId(player.getInventory().getItemInMainHand());
        if (id.isEmpty()) {
            return;
        }
        event.setUseItemInHand(Event.Result.DENY);
        contractService.useScroll(new PlayerID(player.getUniqueId()), id.getAsLong());
    }
}