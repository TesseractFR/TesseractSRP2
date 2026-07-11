package onl.tesseract.srp.skill.domain.model.crafting;


import java.time.Duration;

public record CraftTask(
        QueuedRecipe queuedRecipe,
        Duration unitDuration,
        Duration timeLeft
) {
    public CraftTask(
            QueuedRecipe queuedRecipe,
            Duration unitDuration
    ){
        this(queuedRecipe, unitDuration, unitDuration.multipliedBy(queuedRecipe.getQuantity()));
    }

    public CraftTask tick(Duration duration) {
        Duration newTimeLeft = timeLeft.minus(duration);
        if (newTimeLeft.isNegative()) {
            newTimeLeft = Duration.ZERO;
        }
        return new CraftTask(queuedRecipe, unitDuration, newTimeLeft);
    }

    public void decrementQuantity() {
        queuedRecipe.setQuantity(queuedRecipe.getQuantity() - 1);
    }
}
