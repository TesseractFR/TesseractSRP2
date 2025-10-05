package onl.tesseract.srp.domain.world

enum class SrpWorld(
    val displayName: String,
    val bukkitName: String,
    val resourceWorld: Boolean = false
) {
    Elysea("Elyséa", "elysea"),
    Anterra("Anterra", "anterra", resourceWorld = true),
    Helya("Helya", "helya", resourceWorld = true),
    Nemesis("Némesis", "nemesis", resourceWorld = true),
    GuildWorld("Uh", "guildWorld"),
}