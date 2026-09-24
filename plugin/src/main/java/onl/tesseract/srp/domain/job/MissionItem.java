package onl.tesseract.srp.domain.job;

import onl.tesseract.srp.domain.item.CustomMaterial;

public class MissionItem {
    private final CustomMaterial material;
    private final int quantity;
    private final int minQuality;

    public MissionItem(CustomMaterial material, int quantity, int minQuality) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be > 0");
        if (minQuality <= 0) throw new IllegalArgumentException("Min quality must be > 0");
        this.material = material;
        this.quantity = quantity;
        this.minQuality = minQuality;
    }

    public CustomMaterial getMaterial() { return material; }
    public int getQuantity() { return quantity; }
    public int getMinQuality() { return minQuality; }
}

