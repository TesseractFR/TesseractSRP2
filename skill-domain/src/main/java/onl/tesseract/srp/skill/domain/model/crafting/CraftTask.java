package onl.tesseract.srp.skill.domain.model.crafting;

import lombok.Getter;

import java.time.Duration;

public final class CraftTask {
    @Getter
    private final QueuedRecipe queuedRecipe;
    @Getter
    private final Duration unitDuration;
    private Duration timeLeft;

    public CraftTask(QueuedRecipe queuedRecipe, Duration unitDuration, Duration timeLeft) {
        this.queuedRecipe = queuedRecipe;
        this.unitDuration = unitDuration;
        this.timeLeft = timeLeft;
    }

    public CraftTask(QueuedRecipe queuedRecipe, Duration unitDuration) {
        this(queuedRecipe, unitDuration, unitDuration.multipliedBy(queuedRecipe.getQuantity()));
    }

    public Duration getTimeLeft() {
        return timeLeft;
    }

    public void tick(Duration duration) {
        timeLeft = timeLeft.minus(duration);
        if (timeLeft.isNegative()) {
            timeLeft = Duration.ZERO;
        }
    }

    public void decrementQuantity() {
        queuedRecipe.setQuantity(queuedRecipe.getQuantity() - 1);
    }
}
