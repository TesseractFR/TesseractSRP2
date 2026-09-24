package onl.tesseract.srp.customitem.domain.port.serverside;

import onl.tesseract.srp.customitem.domain.model.CustomItem;
import onl.tesseract.srp.customitem.domain.model.MaterialName;

import java.util.UUID;

public interface InventoryRepository {
    int getCustomQuantity(UUID player, CustomItem customItem);

    int getVanillaQuantity(UUID player, MaterialName materialName);

    void removeCustomItem(UUID player, CustomItem customItem, int amount);

    void removeVanillaItem(UUID player, MaterialName materialName, int amount);

    void addCustomItem(UUID player, CustomItem customItem, int amount);

    void addVanillaItem(UUID player, MaterialName materialName, int amount);


}
