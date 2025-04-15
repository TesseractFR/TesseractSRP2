package onl.tesseract.srp.domain.player

import onl.tesseract.core.title.Title
import org.bukkit.Material

enum class PlayerRank(
    val title: Title,
    val cost: Int,
    val icon: Material,
    val tpDelay: Int,
    val jobMissionSlots: Int,
    val campLevel: Int
) {
    Survivant(
        Title("Survivant", "Survivant", "Survivante"),
        cost = 0,
        icon = Material.WOODEN_SWORD,
        tpDelay = 90,
        jobMissionSlots = 1,
        campLevel = 1
    ),
    Explorateur(
        Title("Explorateur", "Explorateur", "Exploratrice"),
        cost = 500,
        icon = Material.STONE_SWORD,
        tpDelay = 90,
        jobMissionSlots = 2,
        campLevel = 2
    ),
    Aventurier(
        Title("Aventurier", "Aventurier", "Aventurière"),
        cost = 2000,
        icon = Material.IRON_SWORD,
        tpDelay = 80,
        jobMissionSlots = 2,
        campLevel = 3
    ),
    Noble(
        Title("Noble", "Noble", "Noble"),
        cost = 10_000,
        icon = Material.GOLDEN_SWORD,
        tpDelay = 80,
        jobMissionSlots = 3,
        campLevel = 4
    ),
    Baron(
        Title("Baron", "Baron", "Baronne"),
        cost = 50_000,
        icon = Material.DIAMOND_SWORD,
        tpDelay = 70,
        jobMissionSlots = 3,
        campLevel = 5
    ),
    Seigneur(
        Title("Seigneur", "Seigneur", "Seigneur"),
        cost = 250_000,
        icon = Material.NETHERITE_SWORD,
        tpDelay = 70,
        jobMissionSlots = 3,
        campLevel = 6
    ),
    Vicomte(
        Title("Vicomte", "Vicomte", "Vicomtesse"),
        cost = 1_000_000,
        icon = Material.NETHERITE_SWORD,
        tpDelay = 60,
        jobMissionSlots = 4,
        campLevel = 7
    ),
    Comte(
        Title("Comte", "Comte", "Comtesse"),
        cost = 5_000_000,
        icon = Material.NETHERITE_SWORD,
        tpDelay = 60,
        jobMissionSlots = 4,
        campLevel = 8
    ),
    Duc(
        Title("Duc", "Duc", "Duchesse"),
        cost = 15_000_000,
        icon = Material.NETHERITE_SWORD,
        tpDelay = 50,
        jobMissionSlots = 4,
        campLevel = 9
    ),
    Roi(
        Title("Roi", "Roi", "Reine"),
        cost = 50_000_000,
        icon = Material.NETHERITE_SWORD,
        tpDelay = 50,
        jobMissionSlots = 5,
        campLevel = 10
    ),
    Empereur(
        Title("Empereur", "Empereur", "Impératrice"),
        cost = 100_000_000,
        icon = Material.NETHERITE_SWORD,
        tpDelay = 40,
        jobMissionSlots = 5,
        campLevel = 11
    ),
    ;

    fun next(): PlayerRank? {
        return if (this == Empereur) null else entries[ordinal + 1]
    }

    companion object {

        fun getRequiredRankForMissionSlot(slot: Int): PlayerRank {
            try {
                return entries.first { it.jobMissionSlots >= slot }
            } catch (e: Exception) {
                throw IllegalArgumentException("Invalid mission slot $slot")
            }
        }
    }
}