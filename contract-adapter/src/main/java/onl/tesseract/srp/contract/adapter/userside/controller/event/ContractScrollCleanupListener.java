package onl.tesseract.srp.contract.adapter.userside.controller.event;

import onl.tesseract.srp.contract.adapter.utils.ContractConstantes;
import onl.tesseract.srp.contract.adapter.utils.ContractItemUtils;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.port.userside.ContractService;
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
        Item itemEntity = event.getItem();
        ItemStack droppedItem = itemEntity.getItemStack();
        if(!droppedItem.getType().equals(ContractConstantes.BASE_SCROLL.getType())) {
            return;
        }

        Optional<Long> contractId = ContractItemUtils.readContractId(droppedItem);
        if (contractId.isPresent() && contractService.getContract(contractId.get())
                .map(Contract::isExpired).orElse(false)) {
            event.setCancelled(true);
            itemEntity.remove();
            contractService.reportExpiredScrolls(new PlayerId(player.getUniqueId()), 1);
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
            contractService.reportExpiredScrolls(new PlayerId(player.getUniqueId()), removedCount);
        }
    }

    private int removeExpiredScrolls(Inventory inventory) {
        int removedCount = 0;
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack itemStack = inventory.getItem(slot);
            if(itemStack == null) {
                continue;
            }
            if(itemStack.getType() != ContractConstantes.BASE_SCROLL.getType()) {
                continue;
            }
            Optional<Long> contractId = ContractItemUtils.readContractId(itemStack);
            if(contractId.isEmpty()) {
                continue;
            }
            Optional<Contract> contract = contractService.getContract(contractId.get());
            if(contract.isPresent() && contract.get().isExpired()) {
                inventory.setItem(slot, null);
                removedCount++;
            }
        }
        return removedCount;
    }


}