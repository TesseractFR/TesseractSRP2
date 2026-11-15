package onl.tesseract.srp.contract.adapter.userside.controller.event;

import onl.tesseract.srp.contract.adapter.scroll.ContractScrollItems;
import onl.tesseract.srp.contract.adapter.scroll.ContractScrollReader;
import onl.tesseract.srp.contract.adapter.userside.controller.message.ContractMessageFormatter;
import onl.tesseract.srp.contract.domain.port.userside.ContractExpirationService;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Retire les parchemins devenus invalides lors des connexions, ouvertures d'inventaire et ramassages.
 * La validité d'un parchemin est décidée par le domaine.
 */
@Component
public class ContractScrollCleanupListener implements Listener {

    private final ContractExpirationService contractExpirationService;
    private final ContractMessageFormatter contractMessageFormatter;

    public ContractScrollCleanupListener(ContractExpirationService contractExpirationService,
                                         ContractMessageFormatter contractMessageFormatter) {
        this.contractExpirationService = contractExpirationService;
        this.contractMessageFormatter = contractMessageFormatter;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        removeInvalidScrolls(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Item itemEntity = event.getItem();
        if (isInvalidScroll(itemEntity.getItemStack())) {
            event.setCancelled(true);
            itemEntity.remove();
            notifyExpiredScrolls(player, 1);
        }
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        removeInvalidScrolls(event.getInventory());
        if (event.getPlayer() instanceof Player player) {
            removeInvalidScrolls(player);
        }
    }

    private void removeInvalidScrolls(Player player) {
        int removedCount = removeInvalidScrolls(player.getInventory());
        if (removedCount > 0) {
            notifyExpiredScrolls(player, removedCount);
        }
    }

    private void notifyExpiredScrolls(Player player, int count) {
        contractExpirationService.purgeExpired();
        player.sendMessage(contractMessageFormatter.formatScrollsExpired(count));
    }

    private int removeInvalidScrolls(Inventory inventory) {
        int removedCount = 0;
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (isInvalidScroll(inventory.getItem(slot))) {
                inventory.setItem(slot, null);
                removedCount++;
            }
        }
        return removedCount;
    }

    private boolean isInvalidScroll(ItemStack itemStack) {
        return Optional.ofNullable(itemStack)
                .filter(ContractScrollItems::hasScrollType)
                .flatMap(ContractScrollReader::readContractId)
                .map(contractId -> !contractExpirationService.isScrollValid(contractId))
                .orElse(false);
    }
}
