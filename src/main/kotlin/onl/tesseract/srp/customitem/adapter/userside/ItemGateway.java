package onl.tesseract.srp.customitem.adapter.userside;

import onl.tesseract.lib.util.Pair;
import onl.tesseract.srp.customitem.adapter.CustomItemstackMapper;
import onl.tesseract.srp.customitem.domain.model.*;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class ItemGateway {

    private final CustomItemstackMapper itemstackMapper;
    private final CustomItemService customItemService;

    public ItemGateway(CustomItemstackMapper itemstackMapper, CustomItemService customItemService) {
        this.itemstackMapper = itemstackMapper;
        this.customItemService = customItemService;
    }

    public ItemStack getItemStack(ItemTag itemTag){
        return itemstackMapper.getCustomItem(itemTag);
    }

    public ItemStack getItemStack(MaterialName itemMaterial) {
       if(customItemService.isCustomMaterial(itemMaterial)){
           CustomMaterial customMaterial = customItemService.getCustomMaterial(itemMaterial);
           return getItemStack(customMaterial.itemTag());
        }
        return ItemStack.of(Material.valueOf(itemMaterial.value()));

    }

    public int getItemQuantity(UUID player, MaterialName materialName, Quality quality) {
        return customItemService.getItemQuantity(player, materialName, quality);
    }

    public void addItem(UUID player, MaterialName materialName, Quality quality, int amount) {
        customItemService.addItem(player, materialName, quality, amount);
    }

    public void removeItem(UUID player, MaterialName materialName, Quality quality, int amount) {
        customItemService.removeItem(player, materialName, quality, amount);
    }

    public CustomItem getCustomItem(ItemStack itemStack) {
        Pair<MaterialName, Quality> customItemData = itemstackMapper.getCustomItemData(itemStack);
        if (customItemData == null) {
            return null;
        }
        CustomMaterial cm = customItemService.getCustomMaterial(customItemData.getLeft());
        return new CustomItem(cm, customItemData.getRight());
    }

    public Map<MaterialName,CustomMaterial> getCustomMaterials() {
        return customItemService.getCustomMaterials();
    }

    public CustomMaterial getCustomMaterial(@NotNull MaterialName material) {
        return null;
    }
}
