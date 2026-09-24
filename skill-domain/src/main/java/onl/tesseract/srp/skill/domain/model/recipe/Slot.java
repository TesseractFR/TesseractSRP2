package onl.tesseract.srp.skill.domain.model.recipe;

public record Slot(int value) {
    public Slot {
        if (value < 0) {
            throw new IllegalArgumentException("Slot must be >= 0");
        }
    }
}