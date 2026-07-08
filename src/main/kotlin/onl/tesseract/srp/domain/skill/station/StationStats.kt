package onl.tesseract.srp.domain.skill.station

data class StationStats(
    // Stat levels (0 = no upgrade)
    val tierLevel: Int = 0,
    val qualityBonusLevel: Int = 0,
    val successBonusLevel: Int = 0,
    val timeReducLevel: Int = 0,
)
{

    // Quality bonus percentage (0.0 to 1.0)
    val qualityBonusPower: Double
        get() = calculatePower(qualityBonusLevel, 0.02)

    // Success bonus percentage (0.0 to 1.0)
    val successBonusPower: Double
        get() = calculatePower(successBonusLevel, 0.015)

    // Time reduction percentage (0.0 to 1.0)
    val timeReductionPower: Double
        get() = calculatePower(timeReducLevel, 0.01)

    fun getStatPower(statType: StatType): Double {
        return when (statType) {
            StatType.TIER -> tierLevel.toDouble()
            StatType.QUALITY -> qualityBonusPower
            StatType.SUCCESS -> successBonusPower
            StatType.TIME_REDUCTION -> timeReductionPower
            else -> 0.0
        }
    }

    /**
     * Calculates power value based on level using logarithmic scaling.
     * Power increases more slowly at higher levels to prevent overpowering.
     *
     * @param level Current level (0 = no upgrade)
     * @param baseIncrement Base increment per level (diminishing returns applied)
     * @return Power value between 0.0 and 1.0
     */
    private fun calculatePower(level: Int, baseIncrement: Double): Double {
        if (level == 0) return 0.0
        // Logarithmic scaling: level * baseIncrement with diminishing returns
        return minOf(1.0, level * baseIncrement / (1 + (level * 0.1)))
    }

    fun upgradeStat(statType: StatType): StationStats {
        return when (statType) {
            StatType.TIER -> this.copy(tierLevel = tierLevel + 1)
            StatType.QUALITY -> this.copy(qualityBonusLevel = qualityBonusLevel + 1)
            StatType.SUCCESS -> this.copy(successBonusLevel = successBonusLevel + 1)
            StatType.TIME_REDUCTION -> this.copy(timeReducLevel = timeReducLevel + 1)
            else -> this
        }
    }

    fun getStatLevel(statType: onl.tesseract.srp.domain.skill.station.StatType): Int {
        return when (statType) {
            StatType.TIER -> tierLevel
            StatType.QUALITY -> qualityBonusLevel
            StatType.SUCCESS -> successBonusLevel
            StatType.TIME_REDUCTION -> timeReducLevel
            else -> 0
        }
    }
}