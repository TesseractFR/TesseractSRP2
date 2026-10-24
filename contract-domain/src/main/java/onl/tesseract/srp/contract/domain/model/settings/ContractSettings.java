package onl.tesseract.srp.contract.domain.model.settings;

import java.time.Duration;

public record ContractSettings(int bonusCap, Duration scrollTtl, double effortHours, int maxStacks,
                               int stackSize, int discoveryQuantity, Activity activity, Market market) {

    public record Activity(double ticketChance, Duration cooldown) {
    }

    public record Market(double maxPriceRatio) {
    }
}
