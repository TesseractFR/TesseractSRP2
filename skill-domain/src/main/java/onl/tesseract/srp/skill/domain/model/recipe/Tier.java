package onl.tesseract.srp.skill.domain.model.recipe;

public record Tier(int value) {
    public Tier {
        if (value < 1) {
            throw new IllegalArgumentException("Tier must be >= 1");
        }
    }
}