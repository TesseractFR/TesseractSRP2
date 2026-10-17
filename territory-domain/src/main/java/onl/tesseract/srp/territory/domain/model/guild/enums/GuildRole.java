package onl.tesseract.srp.territory.domain.model.guild.enums;

@SuppressWarnings("java:S115")
public enum GuildRole {
    Citoyen, Batisseur, Adjoint, Leader;

    public boolean canWithdrawMoney() { return compareTo(Adjoint) >= 0; }
    public boolean canClaim()         { return compareTo(Adjoint) >= 0; }
    public boolean canSetSpawn()      { return compareTo(Adjoint) >= 0; }
    public boolean canInvite()        { return compareTo(Adjoint) >= 0; }
    public boolean canBuild()         { return compareTo(Batisseur) >= 0; }
}