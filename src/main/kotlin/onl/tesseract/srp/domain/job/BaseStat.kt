package onl.tesseract.srp.domain.job

data class BaseStat(
    val lootChance: Float,
    val moneyGain: Int,
    val xpGain: Int,
    val qualityDistribution: QualityDistribution
)
