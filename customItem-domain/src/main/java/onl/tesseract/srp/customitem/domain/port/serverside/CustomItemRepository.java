package onl.tesseract.srp.customitem.domain.port.serverside;

import onl.tesseract.srp.customitem.domain.model.CustomMaterial;
import org.bukkit.inventory.ItemStack;

public interface CustomItemRepository {
    ItemStack toItemStack(CustomMaterial customMaterial);
}
