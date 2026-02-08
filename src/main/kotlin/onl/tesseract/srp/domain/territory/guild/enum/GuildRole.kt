package onl.tesseract.srp.domain.territory.guild.enum

import net.kyori.adventure.text.format.NamedTextColor

enum class GuildRole(val displayName: String, val color: NamedTextColor) {
    Citoyen("Citoyen", NamedTextColor.GRAY),
    Batisseur("Batisseur", NamedTextColor.GREEN),
    Adjoint("Adjoint", NamedTextColor.GOLD),
    Leader("Leader", NamedTextColor.RED);

    fun canWithdrawMoney(): Boolean = this >= Adjoint
    fun canClaim(): Boolean = this >= Adjoint
    fun canSetSpawn(): Boolean = this >= Adjoint
    fun canInvite(): Boolean = this >= Adjoint
}
