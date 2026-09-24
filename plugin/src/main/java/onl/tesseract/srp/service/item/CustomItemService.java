package onl.tesseract.srp.service.item;

import onl.tesseract.lib.persistantcontainer.NamedspacedKeyProvider;
import onl.tesseract.srp.customitem.domain.model.MaterialName;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.domain.item.CustomItem;
import onl.tesseract.srp.domain.item.CustomMaterial;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.springframework.stereotype.Service;

/**
 * Service for detecting and manipulating custom items in inventories.
 */
@Service
public class CustomItemService {
    private final NamedspacedKeyProvider namespacedKeyProvider;

    public CustomItemService(NamedspacedKeyProvider namespacedKeyProvider) {
        this.namespacedKeyProvider = namespacedKeyProvider;
    }

    /**
     * Checks if the given ItemStack is a custom item.
     */
    public boolean isCustomItem(ItemStack itemStack) {
        return itemStack.getItemMeta() != null
                && itemStack.getItemMeta().getPersistentDataContainer() != null
                && itemStack.getItemMeta().getPersistentDataContainer()
                        .has(namespacedKeyProvider.get("customMaterial"));
    }

    /**
     * Extracts a CustomItem from an ItemStack, or null if not a custom item.
     */
    public CustomItem getCustomItemStack(ItemStack itemStack) {
        if (!isCustomItem(itemStack)) {
            return null;
        }
        var dataContainer = itemStack.getItemMeta().getPersistentDataContainer();
        String matStr = dataContainer.get(namespacedKeyProvider.get("customMaterial"), PersistentDataType.STRING);
        String qualityStr = dataContainer.get(namespacedKeyProvider.get("quality"), PersistentDataType.STRING);
        if (matStr == null || qualityStr == null) {
            return null;
        }
        var mat = new MaterialName(matStr);
        var quality = Quality.valueOf(qualityStr);
        var customMaterial = CustomMaterial.getByMaterialName(mat);
        if (customMaterial == null) {
            return null;
        }
        return new CustomItem(customMaterial, quality, itemStack.getAmount());
    }

    /**
     * Removes custom items from an inventory matching the given material and minimum quality.
     * @return the number of items removed
     */
    public int removeCustomItems(Inventory inventory, MaterialName material, Quality minQuality, int amountToRemove) {
        int remaining = amountToRemove;
        int removed = 0;

        for (int index = 0; index < inventory.getSize(); index++) {
            if (remaining <= 0) {
                break;
            }
            ItemStack item = inventory.getItem(index);
            if (item != null && isCustomItem(item)) {
                CustomItem stack = getCustomItemStack(item);
                if (stack != null && stack.getQuality().ordinal() >= minQuality.ordinal()) {
                    int amount = item.getAmount();
                    int toRemove = Math.min(remaining, amount);

                    if (toRemove >= amount) {
                        inventory.setItem(index, null);
                    } else {
                        item.setAmount(amount - toRemove);
                    }

                    removed += toRemove;
                    remaining -= toRemove;
                }
            }
        }

        return removed;
    }
}

