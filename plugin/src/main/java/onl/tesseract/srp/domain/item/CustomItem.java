package onl.tesseract.srp.domain.item;

import onl.tesseract.srp.common.domain.model.enums.Quality;

public class CustomItem {
    private final CustomMaterial material;
    private final Quality quality;
    private final int quantity;

    public CustomItem(CustomMaterial material, Quality quality, int quantity) {
        this.material = material;
        this.quality = quality;
        this.quantity = quantity;
    }

    public CustomMaterial getMaterial() { return material; }
    public Quality getQuality() { return quality; }
    public int getQuantity() { return quantity; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomItem that = (CustomItem) o;
        return quantity == that.quantity && material == that.material && quality == that.quality;
    }

    @Override
    public int hashCode() {
        int result = material.hashCode();
        result = 31 * result + quality.hashCode();
        result = 31 * result + quantity;
        return result;
    }
}

