package onl.tesseract.srp.domain.job;

import onl.tesseract.srp.common.domain.model.enums.Quality;
import java.util.Random;

public class BaseStat {
    private static final Random random = new Random();

    private final float lootChance;
    private final int moneyGain;
    private final int xpGain;
    private final QualityDistribution qualityDistribution;

    public BaseStat(float lootChance, int moneyGain, int xpGain, QualityDistribution qualityDistribution) {
        this.lootChance = lootChance;
        this.moneyGain = moneyGain;
        this.xpGain = xpGain;
        this.qualityDistribution = qualityDistribution;
    }

    public float getLootChance() { return lootChance; }
    public int getMoneyGain() { return moneyGain; }
    public int getXpGain() { return xpGain; }
    public QualityDistribution getQualityDistribution() { return qualityDistribution; }

    public boolean randomizeLootChance() {
        return random.nextFloat() < lootChance;
    }

    public Quality generateQuality() {
        return Quality.POOR;
    }

    public BaseStat multiplyLootChance(float coef) {
        return new BaseStat(lootChance * (1 + coef), moneyGain, xpGain, qualityDistribution);
    }

    public BaseStat multiplyMoneyGain(float coef) {
        return new BaseStat(lootChance, (int)(moneyGain * (1 + coef)), xpGain, qualityDistribution);
    }

    public BaseStat addQualityMean(int coef) {
        return new BaseStat(
            lootChance,
            moneyGain,
            xpGain,
            new QualityDistribution((int)(qualityDistribution.getExpectation() + coef), qualityDistribution.getStddev())
        );
    }
}

