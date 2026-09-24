package onl.tesseract.srp.skill.domain.model.station;

import onl.tesseract.srp.skill.domain.model.skill.SkillName;
import lombok.NonNull;

import java.util.UUID;

public record StationKey(
            @NonNull UUID territoryId,
            @NonNull SkillName skillName
) {
    public StationKey(SkillName skillName) {
        this(UUID.randomUUID(), skillName);
    }
}