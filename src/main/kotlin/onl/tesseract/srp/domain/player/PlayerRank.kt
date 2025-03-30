package onl.tesseract.srp.domain.player

import onl.tesseract.core.title.Title
import org.bukkit.Material

enum class PlayerRank(val title: Title, val cost: Int, val icon: Material) {
    Survivant(Title("Survivant", "Survivant", "Survivante"), 0, Material.WOODEN_SWORD),
    Explorateur(Title("Explorateur", "Explorateur", "Exploratrice"), 500, Material.STONE_SWORD),
    Aventurier(Title("Aventurier", "Aventurier", "Aventurière"), 2000, Material.IRON_SWORD),
    Noble(Title("Noble", "Noble", "Noble"), 10_000, Material.GOLDEN_SWORD),
    Baron(Title("Baron", "Baron", "Baronne"), 50_000, Material.DIAMOND_SWORD),
    Seigneur(Title("Seigneur", "Seigneur", "Seigneur"), 250_000, Material.NETHERITE_SWORD),
    Vicomte(Title("Vicomte", "Vicomte", "Vicomtesse"), 1_000_000, Material.NETHERITE_SWORD),
    Comte(Title("Comte", "Comte", "Comtesse"), 5_000_000, Material.NETHERITE_SWORD),
    Duc(Title("Duc", "Duc", "Duchesse"), 15_000_000, Material.NETHERITE_SWORD),
    Roi(Title("Roi", "Roi", "Reine"), 50_000_000, Material.NETHERITE_SWORD),
    Empereur(Title("Empereur", "Empereur", "Impératrice"), 100_000_000, Material.NETHERITE_SWORD),
    ;

    fun next(): PlayerRank? {
        return if (this == Empereur) null else entries[ordinal + 1]
    }
}