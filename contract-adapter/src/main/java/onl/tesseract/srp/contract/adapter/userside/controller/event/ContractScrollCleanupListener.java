package onl.tesseract.srp.contract.adapter.userside.controller.event;

import onl.tesseract.srp.contract.adapter.utils.ContractScrollItem;
import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.port.userside.ContractService;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ContractScrollCleanupListener implements Listener {

    private final ContractService contractService;

    public ContractScrollCleanupListener(ContractService contractService) {
        this.contractService = contractService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        cleanPlayer(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Item droppedItem = event.getItem();
        if (ContractScrollItem.isExpired(droppedItem.getItemStack(), Instant.now())) {
            event.setCancelled(true);
            droppedItem.remove();
            contractService.reportExpiredScrolls(new PlayerID(player.getUniqueId()), 1);
        }
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        if (removeExpiredScrolls(event.getInventory()) > 0) {
            contractService.purgeExpired();
        }
        if (event.getPlayer() instanceof Player player) {
            cleanPlayer(player);
        }
    }

    private void cleanPlayer(Player player) {
        int removedCount = removeExpiredScrolls(player.getInventory());
        if (removedCount > 0) {
            contractService.reportExpiredScrolls(new PlayerID(player.getUniqueId()), removedCount);
        }
    }

    private static int removeExpiredScrolls(Inventory inventory) {
        Instant now = Instant.now();
        int removedCount = 0;
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (ContractScrollItem.isExpired(inventory.getItem(slot), now)) {
                inventory.setItem(slot, null);
                removedCount++;
            }
        }
        return removedCount;
    }
}