package onl.tesseract.srp.domain.job;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.srp.domain.item.CustomMaterial;

public record GenericMaterialJobBonus(CustomMaterial material, JobBonusType type, float value) implements JobBonus {

    @Override
    public float getLootChanceBonus(JobHarvestEvent event) {
        return (event.getMaterial() == this.material && type == JobBonusType.LootChance) ? value : 0f;
    }

    @Override
    public float getMoneyBonus(JobHarvestEvent event) {
        return (event.getMaterial() == this.material && type == JobBonusType.Money) ? value : 0f;
    }

    @Override
    public int getQualityBonus(JobHarvestEvent event) {
        return (event.getMaterial() == this.material && type == JobBonusType.Quality) ? (int) value : 0;
    }

    @Override
    public Component getDescription() {
        return Component.text(material.getDisplayName(), NamedTextColor.BLUE)
                .append(Component.text(" : " + type + " +" + (int) (value * 100) + "%", NamedTextColor.GRAY));
    }
}

