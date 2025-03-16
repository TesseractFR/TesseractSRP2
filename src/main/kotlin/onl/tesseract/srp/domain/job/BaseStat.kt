package onl.tesseract.srp.domain.job

import java.util.*

val random = Random()

data class BaseStat(
    val lootChance: Float,
    val moneyGain: Int,
    val xpGain: Int,
    val qualityDistribution: QualityDistribution
) {

    fun randomizeLootChance(): Boolean {
        return random.nextFloat() < lootChance
    }

    fun generateQuality(): Int {
        val gaussian = random.nextGaussian() * qualityDistribution.stddev + qualityDistribution.expectation
        return gaussian.toInt().coerceIn(1, 100)
    }

    fun multiplyLootChance(coef: Float): BaseStat {
        return BaseStat(lootChance * (1 + coef), moneyGain, xpGain, qualityDistribution)
    }

    fun multiplyMoneyGain(coef: Float): BaseStat {
        return BaseStat(
            lootChance, (moneyGain * (1 + coef)).toInt(), xpGain, qualityDistribution
        )
    }

    fun addQualityMean(coef: Int): BaseStat {
        return BaseStat(
            lootChance,
            moneyGain,
            xpGain,
            QualityDistribution((qualityDistribution.expectation + coef).toInt(), qualityDistribution.stddev)
        )
    }
}
