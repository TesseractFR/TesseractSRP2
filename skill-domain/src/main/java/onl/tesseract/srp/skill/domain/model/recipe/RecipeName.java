package onl.tesseract.srp.skill.domain.model.recipe;

import org.apache.commons.lang3.StringUtils;

public record RecipeName(
    String value
) {
    public RecipeName {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("Recipe name cannot be null or empty");
        }
    }
}
