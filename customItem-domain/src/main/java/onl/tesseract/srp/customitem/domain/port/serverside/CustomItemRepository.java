package onl.tesseract.srp.customitem.domain.port.serverside;

import onl.tesseract.srp.customitem.domain.model.CustomMaterial;
import onl.tesseract.srp.customitem.domain.model.MaterialName;
import org.bukkit.inventory.ItemStack;

public interface CustomItemRepository {
    ItemStack toCustomItemStack(CustomMaterial customMaterial);

    ItemStack toVanillaItemStack(MaterialName materialName);

}
