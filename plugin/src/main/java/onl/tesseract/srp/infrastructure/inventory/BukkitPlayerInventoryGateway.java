package onl.tesseract.srp.infrastructure.inventory;

import onl.tesseract.srp.domain.port.PlayerInventoryPort;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Bukkit-based implementation of the PlayerInventoryPort.
 */
@Component
public class BukkitPlayerInventoryGateway implements PlayerInventoryPort {

    @Override
    public int getItemNumber(UUID player, ItemStack item) {
        org.bukkit.entity.Player bukkitPlayer = Bukkit.getPlayer(player);
        if (bukkitPlayer == null) {
            return 0;
        }
        var inventory = bukkitPlayer.getInventory();
        int count = 0;
        for (var content : inventory.getContents()) {
            if (content != null && content.isSimilar(item)) {
                count += content.getAmount();
            }
        }
        return count;
    }

    @Override
    public void removeItems(UUID player, ItemStack item, int amount) {
        org.bukkit.entity.Player bukkitPlayer = Bukkit.getPlayer(player);
        if (bukkitPlayer == null) {
            return;
        }
        var inventory = bukkitPlayer.getInventory();
        int remaining = amount;
        for (int i = 0; i < inventory.getSize(); i++) {
            var content = inventory.getItem(i);
            if (content != null && content.isSimilar(item)) {
                int toRemove = Math.min(remaining, content.getAmount());
                content.setAmount(content.getAmount() - toRemove);
                remaining -= toRemove;
                if (remaining <= 0) {
                    break;
                }
            }
        }
    }

    @Override
    public void giveItems(UUID player, List<ItemStack> items) {
        org.bukkit.entity.Player bukkitPlayer = Bukkit.getPlayer(player);
        if (bukkitPlayer == null) {
            return;
        }
        var remaining = bukkitPlayer.getInventory().addItem(items.toArray(new ItemStack[0]));
        if (!remaining.isEmpty()) {
            remaining.values().forEach(stack ->
                    bukkitPlayer.getWorld().dropItemNaturally(bukkitPlayer.getLocation(), stack));
        }
    }
}

