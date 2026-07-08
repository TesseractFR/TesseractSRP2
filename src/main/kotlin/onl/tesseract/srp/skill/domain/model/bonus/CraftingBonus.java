package onl.tesseract.srp.skill.domain.model.bonus;

public record CraftingBonus(
    StationBonus craftingStationBonus,
    Bonus garbageRefundBonus,
    Bonus craftRefundBonus,
    Bonus recoverySuccessBonus
){}
