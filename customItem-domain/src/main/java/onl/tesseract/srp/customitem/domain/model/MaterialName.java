package onl.tesseract.srp.customitem.domain.model;

import org.apache.commons.lang3.StringUtils;

public record MaterialName(
        String value
) {
    public MaterialName {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("Material cannot be null or empty");
        }
    }
}