package onl.tesseract.srp.domain.territory.guild.enum

import net.kyori.adventure.text.format.NamedTextColor

enum class GuildRank(
    val title: String,
    val minLevel: Int,
    val cost: Int,
    val maxChunksNumber: Int,
    val maxMembersNumber: Int,
    val color: NamedTextColor
) {
    HAMEAU("Hameau", 1, 0, 9, 3, NamedTextColor.WHITE),
    COMMUNE("Commune", 2, 5_000, 25, 10, NamedTextColor.DARK_GREEN),
    VILLAGE("Village", 4, 15_000, 50, 20, NamedTextColor.GREEN),
    VILLE("Ville", 7, 50_000, 100, 30, NamedTextColor.AQUA),
    CITE("Cité", 10, 150_000, 250, 40, NamedTextColor.LIGHT_PURPLE),
    CAPITALE("Capitale", 14, 400_000, 500, 50, NamedTextColor.GOLD);

    fun next(): GuildRank? = entries.getOrNull(ordinal + 1)
}
