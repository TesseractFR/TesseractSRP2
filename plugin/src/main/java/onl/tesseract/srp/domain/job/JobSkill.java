package onl.tesseract.srp.domain.job;

import onl.tesseract.srp.domain.item.CustomMaterial;
import org.bukkit.Material;

/**
 * Represents a skill that can be unlocked by a player in a job.
 */
public enum JobSkill {
    MINEUR_LOOT_CHANCE_1(
            EnumJob.Mineur,
            "Chance 1",
            Material.RABBIT_FOOT,
            null,
            1,
            new GenericMaterialJobBonus(CustomMaterial.STEEL, JobBonusType.LootChance, 0.15f)
    ),
    MINEUR_QUALITY_1(
            EnumJob.Mineur,
            "Qualité 1",
            Material.EXPERIENCE_BOTTLE,
            null,
            1,
            new GenericMaterialJobBonus(CustomMaterial.STEEL, JobBonusType.Quality, 0.5f)
    ),
    MINEUR_MONEY_1(
            EnumJob.Mineur,
            "Money 1",
            Material.GOLD_NUGGET,
            null,
            1,
            new GenericMaterialJobBonus(CustomMaterial.STEEL, JobBonusType.Money, 0.1f)
    );

    private final EnumJob root;
    private final String displayName;
    private final Material icon;
    private final JobSkill parent;
    private final int cost;
    private final JobBonus bonus;

    JobSkill(EnumJob root, String displayName, Material icon, JobSkill parent, int cost, JobBonus bonus) {
        this.root = root;
        this.displayName = displayName;
        this.icon = icon;
        this.parent = parent;
        this.cost = cost;
        this.bonus = bonus;
    }

    public EnumJob getRoot() {
        return root;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public JobSkill getParent() {
        return parent;
    }

    public int getCost() {
        return cost;
    }

    public JobBonus getBonus() {
        return bonus;
    }
}

