package onl.tesseract.srp.customItem.adapter.serverside;


import onl.tesseract.lib.persistantcontainer.NamedspacedKeyProvider;
import onl.tesseract.lib.util.Pair;
import onl.tesseract.srp.common.adapter.utils.QualityAdapter;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.customitem.domain.model.*;
import onl.tesseract.srp.customitem.domain.port.serverside.CustomItemRepository;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.srp.customitem.domain.port.serverside.MoneyRepository;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.util.List;
import java.util.ArrayList;

@org.springframework.stereotype.Component
public class CustomItemstackMapper implements CustomItemRepository {

    private final ItemAdderGateway itemAdderGateway;
    private final NamedspacedKeyProvider namedspacedKeyProvider;
    private final MoneyRepository moneyRepository;

    public CustomItemstackMapper(ItemAdderGateway itemAdderGateway, NamedspacedKeyProvider namedspacedKeyProvider, MoneyRepository moneyRepository) {
        this.itemAdderGateway = itemAdderGateway;
        this.namedspacedKeyProvider = namedspacedKeyProvider;
        this.moneyRepository = moneyRepository;
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
        CustomMaterial material = customItem.material();
        RarityAdapter rarity = RarityAdapter.from(material.rarity());
        itemStack.editMeta(itemMeta -> {
                    itemMeta.displayName(Component.text(material.displayName().value(), rarity.color()));
                    PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
                    dataContainer.set(namedspacedKeyProvider.get("customMaterial"), PersistentDataType.STRING, customItem.material().name().value());
                    dataContainer.set(namedspacedKeyProvider.get("quality"), PersistentDataType.STRING, customItem.quality().toString());
                }
        );
        List<Component> lore = itemStack.lore() == null ? new ArrayList<>() : new ArrayList<>(itemStack.lore());
        QualityAdapter quality = QualityAdapter.from(customItem.quality());
        lore.add(Component.text("Rareté : ", NamedTextColor.GRAY)
                .append(Component.text(rarity.label(), rarity.color())));
        lore.add(Component.text("Qualité : ", NamedTextColor.GRAY)
                .append(Component.text(quality.label(), quality.color())));
        if (moneyRepository.hasValue(material.name())) {
            String value = moneyRepository.getValue(material.name(), customItem.quality()).value().stripTrailingZeros().toPlainString();
            lore.add(Component.text("Valeur : ", NamedTextColor.GRAY)
                    .append(Component.text(value, NamedTextColor.GOLD)));
        }
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
