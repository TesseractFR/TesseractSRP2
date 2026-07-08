package onl.tesseract.srp.customitem.domain.port.serverside;

import onl.tesseract.srp.customitem.domain.model.CustomItem;
import onl.tesseract.srp.customitem.domain.model.MaterialName;

import java.util.UUID;

public interface InventoryRepository {
    int getCustomQuantity(UUID player, CustomItem customItem);

    int getVanillaQuantity(UUID player, MaterialName materialName);
}
