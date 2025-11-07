package onl.tesseract.srp.domain.territory.guild.enum


enum class GuildRole {
    Citoyen,
    Batisseur,
    Adjoint,
    Leader,
    ;

    fun canWithdrawMoney(): Boolean = this >= Adjoint
    fun canClaim(): Boolean = this >= Adjoint
}