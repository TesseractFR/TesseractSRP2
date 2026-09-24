package onl.tesseract.srp.skill.domain.model.recipe;

public record IngredientSlot(int value) {
    public IngredientSlot {
        if (value < 0) {
            throw new IllegalArgumentException("Ingredient slot value cannot be negative");
        }
    }
}
