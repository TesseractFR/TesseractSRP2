package onl.tesseract.srp.common.domain.model.item;

public record MaterialName(
        String value
) {
    public MaterialName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Material cannot be null or empty");
        }
    }
}
