package onl.tesseract.srp.domain.port;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

/**
 * Port for managing a player's inventory operations.
 */
public interface PlayerInventoryPort {
    int getItemNumber(UUID player, ItemStack item);
    void removeItems(UUID player, ItemStack item, int amount);
    void giveItems(UUID player, List<ItemStack> items);
}

