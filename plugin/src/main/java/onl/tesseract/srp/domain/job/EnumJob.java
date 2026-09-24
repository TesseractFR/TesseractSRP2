package onl.tesseract.srp.domain.job;

import org.bukkit.Material;

public enum EnumJob {
    Mineur("Mineur", Material.IRON_PICKAXE),
    Terrassier("Terrassier", Material.IRON_SHOVEL),
    Bucheron("Bucheron", Material.IRON_AXE),
    Pecheur("Pecheur", Material.FISHING_ROD),
    Chasseur("Chasseur", Material.BOW),
    Gardien("Gardien", Material.IRON_SWORD),
    Agriculteur("Agriculteur", Material.IRON_HOE),
    Fleuriste("Fleuriste", Material.RED_TULIP);

    private final String displayName;
    private final Material icon;

    EnumJob(String displayName, Material icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() { return displayName; }
    public Material getIcon() { return icon; }
}

