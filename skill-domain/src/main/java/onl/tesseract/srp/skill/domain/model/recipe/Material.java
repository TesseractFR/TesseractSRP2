package onl.tesseract.srp.skill.domain.model.recipe;

import org.apache.commons.lang3.StringUtils;

public record Material(
    String value
){
    public Material{
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("Material cannot be null or empty");
        }
    }
}