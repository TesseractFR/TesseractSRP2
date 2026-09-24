package onl.tesseract.srp.common.domain.model.enums;

public enum Quality {
    POOR,
    NORMAL,
    GOOD,
    VERY_GOOD,
    EXCEPTIONAL;

    public Quality next() {
        return Quality.values()[Math.min(ordinal() + 1, values().length - 1)];
    }
}
