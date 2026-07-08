package onl.tesseract.srp.skill.domain.model.station;

import onl.tesseract.srp.skill.domain.model.skill.SkillName;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record StationKey(
            @NotNull UUID territoryId,
            @NotNull SkillName skillName
) {
    public StationKey(SkillName skillName) {
        this(UUID.randomUUID(), skillName);
    }
}