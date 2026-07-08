package onl.tesseract.srp.customitem.adapter;

import onl.tesseract.lib.persistantcontainer.NamedspacedKeyProvider;
import onl.tesseract.srp.customitem.domain.model.CustomItem;
import onl.tesseract.srp.customitem.domain.model.ItemTag;
import onl.tesseract.srp.infrastructure.item.ItemAdderGateway;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.springframework.stereotype.Component;

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
        return itemStack;
    }

    public boolean isCustom(ItemStack itemStack) {
        return itemStack.getItemMeta().getPersistentDataContainer()
                .get(namedspacedKeyProvider.get("customMaterial"),PersistentDataType.STRING) != null;
    }
}
