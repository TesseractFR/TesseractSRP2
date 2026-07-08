package onl.tesseract.srp.customitem.adapter.userside;

import onl.tesseract.srp.customitem.adapter.CustomItemstackMapper;
import onl.tesseract.srp.customitem.domain.model.CustomMaterial;
import onl.tesseract.srp.customitem.domain.model.MaterialName;
import onl.tesseract.srp.customitem.domain.model.Quality;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ItemGateway {

    private final CustomItemstackMapper itemstackMapper;
    private final CustomItemService customItemService;

    public ItemGateway(CustomItemstackMapper itemstackMapper, CustomItemService customItemService) {
        this.itemstackMapper = itemstackMapper;
        this.customItemService = customItemService;
    }

    public ItemStack getItemStack(MaterialName itemMaterial) {
       if(customItemService.isCustomMaterial(itemMaterial)){
           CustomMaterial customMaterial = customItemService.getCustomMaterial(itemMaterial);
           return itemstackMapper.getCustomItem(customMaterial.itemTag());
        }
        return ItemStack.of(Material.valueOf(itemMaterial.value()));

    }

    public int getItemQuantity(UUID player, MaterialName materialName, Quality quality) {
        return customItemService.getItemQuantity(player, materialName, quality);
    }
}
