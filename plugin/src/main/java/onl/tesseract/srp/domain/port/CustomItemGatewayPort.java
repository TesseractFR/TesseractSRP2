package onl.tesseract.srp.domain.port;

import org.bukkit.inventory.ItemStack;

/**
 * Port for accessing custom items.
 */
public interface CustomItemGatewayPort {
    ItemStack getCustomItem(String namespaceId);
    boolean isCustomItem(ItemStack itemStack);
}

