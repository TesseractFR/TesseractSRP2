package onl.tesseract.srp.domain.player

import org.bukkit.Material

enum class PlayerRank(val cost: Int, val icon: Material) {
    Survivant(0, Material.WOODEN_SWORD),
    Explorateur(500, Material.STONE_SWORD),
    Aventurier(2000, Material.IRON_SWORD),
    Noble(10_000, Material.GOLDEN_SWORD),
    Baron(50_000, Material.DIAMOND_SWORD),
    Seigneur(250_000, Material.NETHERITE_SWORD),
    Vicomte(1_000_000, Material.NETHERITE_SWORD),
    Comte(5_000_000, Material.NETHERITE_SWORD),
    Duc(15_000_000, Material.NETHERITE_SWORD),
    Roi(50_000_000, Material.NETHERITE_SWORD),
    Empereur(100_000_000, Material.NETHERITE_SWORD),
    ;

    fun next(): PlayerRank? {
        return if (this == Empereur) null else entries[ordinal + 1]
    }
}