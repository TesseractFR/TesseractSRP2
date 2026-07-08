package onl.tesseract.srp.skill.domain.model.skill;

import org.apache.commons.lang3.StringUtils;

public record SkillName(
        String value
) {
    public SkillName{
        if(StringUtils.isBlank(value)){
            throw new IllegalArgumentException("Skill name cannot be blank");
        }
    }
}
