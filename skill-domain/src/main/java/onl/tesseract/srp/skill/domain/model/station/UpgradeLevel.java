package onl.tesseract.srp.skill.domain.model.station;

public record UpgradeLevel(
        int value
) {
    public UpgradeLevel {
        if (value < 0) {
            throw new IllegalArgumentException("Upgrade level cannot be negative");
        }
    }
}
