package onl.tesseract.srp.domain.skill.station

/**
 * Represents a crafting station with improvable statistics.
 * Stats synchronize across all stations of the same skill type within a guild.
 * Territory is the Guild UUID - determined at runtime when player clicks the station.
 */
data class CraftingStation(
    val key: StationKey,
    val stationStats: StationStats,
) {
    // Minimum tier for recipes
    val baseTier: Int
        get() = 1 + stationStats.tierLevel

    fun upgradeStat(statType: StatType): CraftingStation {
        return this.copy(stationStats = stationStats.upgradeStat(statType))
    }
    
    fun getStatLevel(statType: StatType): Int {
        return stationStats.getStatLevel(statType)
    }

    fun getStatPower(statType: StatType): Double {
        return stationStats.getStatPower(statType)
    }
}

