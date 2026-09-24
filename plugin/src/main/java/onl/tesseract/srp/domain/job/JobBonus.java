package onl.tesseract.srp.domain.job;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.srp.domain.item.CustomMaterial;

public interface JobBonus {
    float getLootChanceBonus(JobHarvestEvent event);
    float getMoneyBonus(JobHarvestEvent event);
    int getQualityBonus(JobHarvestEvent event);
    Component getDescription();
}

