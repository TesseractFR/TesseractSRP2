package onl.tesseract.srp.domain.territory.guild

interface GuildLevelHolder {
    val level: Int
    val xp: Int
    fun getXpForNextLevel(): Int
}