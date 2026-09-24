package onl.tesseract.srp.domain.item;

import onl.tesseract.srp.customitem.domain.model.MaterialName;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public enum CustomMaterial {
    STEEL("Acier", new MaterialName("steel_ingot"), java.util.List.of(), Rarity.Common);

    private final String displayName;
    private final MaterialName materialName;
    private final java.util.List<CustomMaterialSource> dropSource;
    private final Rarity rarity;

    CustomMaterial(String displayName, MaterialName materialName, java.util.List<CustomMaterialSource> dropSource, Rarity rarity) {
        this.displayName = displayName;
        this.materialName = materialName;
        this.dropSource = dropSource;
        this.rarity = rarity;
    }

    public String getDisplayName() { return displayName; }
    public MaterialName getMaterialName() { return materialName; }
    public java.util.List<CustomMaterialSource> getDropSource() { return dropSource; }
    public Rarity getRarity() { return rarity; }

    public static CustomMaterial getByMaterialName(MaterialName materialName) {
        for (CustomMaterial value : values()) {
            if (value.materialName.value().equals(materialName.value())) {
                return value;
            }
        }
        return null;
    }
}

