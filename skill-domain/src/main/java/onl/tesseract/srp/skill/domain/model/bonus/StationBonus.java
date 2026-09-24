package onl.tesseract.srp.skill.domain.model.bonus;

public record StationBonus(
    Bonus qualityBonus,
    Bonus successBonus,
    Bonus timeReduction
) {
}
