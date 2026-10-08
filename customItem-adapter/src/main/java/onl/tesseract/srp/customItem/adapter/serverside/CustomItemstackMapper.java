package onl.tesseract.srp.customItem.adapter.serverside;


import onl.tesseract.lib.persistantcontainer.NamedspacedKeyProvider;
import onl.tesseract.lib.util.Pair;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.customitem.domain.model.*;
import onl.tesseract.srp.customitem.domain.port.serverside.CustomItemRepository;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomItemstackMapper implements CustomItemRepository {

    private final ItemAdderGateway itemAdderGateway;
    private final NamedspacedKeyProvider namedspacedKeyProvider;

    public CustomItemstackMapper(ItemAdderGateway itemAdderGateway, NamedspacedKeyProvider namedspacedKeyProvider) {
        this.itemAdderGateway = itemAdderGateway;
        this.namedspacedKeyProvider = namedspacedKeyProvider;
    }

    public ItemStack toCustomItemStack(CustomMaterial material) {
        return itemAdderGateway.getCustomItem(material.itemTag().value());
    }

    @Override
    public ItemStack toVanillaItemStack(MaterialName materialName) {
        Material material = Material.getMaterial(materialName.value());
        if(material == null)return null;
        return ItemStack.of(material);
    }

    public ItemStack toItemStack(CustomItem customItem) {
        ItemStack itemStack = toCustomItemStack(customItem.material());
        itemStack.editMeta(itemMeta -> {
                    PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
                    dataContainer.set(namedspacedKeyProvider.get("customMaterial"), PersistentDataType.STRING, customItem.material().name().value());
                    dataContainer.set(namedspacedKeyProvider.get("quality"), PersistentDataType.STRING, customItem.quality().toString());
                }
        );
        List lore = itemStack.lore();
        lore.add(net.kyori.adventure.text.Component.text("Quality: " + customItem.quality()));
        itemStack.lore(lore);
        return itemStack;
    }

    public boolean isCustom(ItemStack itemStack) {
        return itemStack.getItemMeta().getPersistentDataContainer()
                .get(namedspacedKeyProvider.get("customMaterial"),PersistentDataType.STRING) != null;
    }


    public Pair<MaterialName, Quality> getCustomItemData(ItemStack itemStack) {
        if(!isCustom(itemStack))return null;

        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        String customMaterial = container.get(namedspacedKeyProvider.get("customMaterial"),PersistentDataType.STRING);
        String quality = container.get(namedspacedKeyProvider.get("quality"),PersistentDataType.STRING);
        return new Pair<>(new MaterialName(customMaterial), Quality.valueOf(quality));
    }
}
