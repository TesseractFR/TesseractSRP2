package onl.tesseract.srp.skill.domain.model.bonus;

public record Bonus(
        double value
) {
    public Bonus{
        if (value < 0) {
            throw new IllegalArgumentException("Quality bonus value cannot be negative");
        }
    }
}
