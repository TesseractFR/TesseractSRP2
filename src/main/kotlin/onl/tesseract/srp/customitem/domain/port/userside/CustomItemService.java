package onl.tesseract.srp.customitem.domain.port.userside;

import onl.tesseract.srp.customitem.domain.model.CustomItem;
import onl.tesseract.srp.customitem.domain.model.CustomMaterial;
import onl.tesseract.srp.customitem.domain.model.MaterialName;
import onl.tesseract.srp.customitem.domain.model.Quality;
import onl.tesseract.srp.customitem.domain.port.serverside.CustumMaterialRepository;
import onl.tesseract.srp.customitem.domain.port.serverside.InventoryRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomItemService {

    private final CustumMaterialRepository custumMaterialRepository;
    private final InventoryRepository inventoryRepository;

    private final Map<MaterialName, CustomMaterial> customItems = new HashMap<>();

    public CustomItemService(CustumMaterialRepository custumMaterialRepository, InventoryRepository inventoryRepository) {
        this.custumMaterialRepository = custumMaterialRepository;
        this.inventoryRepository = inventoryRepository;
        loadAll();
    }

    public void loadAll() {
        customItems.clear();
        for (CustomMaterial material : custumMaterialRepository.findAll()) {
            customItems.put(material.name(), material);
        }
    }

    public boolean isCustomMaterial(MaterialName itemMaterial) {
        return customItems.containsKey(itemMaterial);
    }

    public CustomMaterial getCustomMaterial(MaterialName itemMaterial) {
        return customItems.get(itemMaterial);
    }

    public int getItemQuantity(UUID player, MaterialName materialName, Quality quality) {
        if (isCustomMaterial(materialName)) {
            CustomMaterial customMaterial = getCustomMaterial(materialName);
            CustomItem customItem = new CustomItem(customMaterial, quality);
            return inventoryRepository.getCustomQuantity(player, customItem);
        }
        return inventoryRepository.getVanillaQuantity(player, materialName);
    }
}
