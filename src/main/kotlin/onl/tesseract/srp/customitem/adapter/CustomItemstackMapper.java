package onl.tesseract.srp.customitem.adapter;

import onl.tesseract.lib.persistantcontainer.NamedspacedKeyProvider;
import onl.tesseract.lib.util.Pair;
import onl.tesseract.srp.customitem.domain.model.*;
import onl.tesseract.srp.infrastructure.item.ItemAdderGateway;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomItemstackMapper {

    private final ItemAdderGateway itemAdderGateway;
    private final NamedspacedKeyProvider namedspacedKeyProvider;

    public CustomItemstackMapper(ItemAdderGateway itemAdderGateway, NamedspacedKeyProvider namedspacedKeyProvider) {
        this.itemAdderGateway = itemAdderGateway;
        this.namedspacedKeyProvider = namedspacedKeyProvider;
    }

    public ItemStack getCustomItem(ItemTag itemTag) {
        return itemAdderGateway.getCustomItem(itemTag.value());
    }

    public ItemStack toItemStack(CustomItem customItem) {
        ItemStack itemStack = getCustomItem(customItem.material().itemTag());
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
