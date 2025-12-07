package onl.tesseract.srp.domain.territory.guild.enum

enum class GuildRank(
    val title: String,
    val minLevel: Int,
    val cost: Int
) {
    HAMEAU   ("Hameau",   minLevel = 1,  cost = 0),
    COMMUNE ("Commune", minLevel = 2,  cost = 5_000),
    VILLAGE  ("Village",  minLevel = 4,  cost = 15_000),
    VILLE    ("Ville",    minLevel = 7,  cost = 50_000),
    CITE     ("Cité",     minLevel = 10, cost = 150_000),
    CAPITALE ("Capitale", minLevel = 14, cost = 400_000);

    fun next(): GuildRank? = entries.getOrNull(ordinal + 1)
}
