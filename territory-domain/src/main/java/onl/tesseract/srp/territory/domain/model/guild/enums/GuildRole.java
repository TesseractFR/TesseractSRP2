package onl.tesseract.srp.territory.domain.model.guild.enums;

public enum GuildRole {
    Citoyen,
    Batisseur,
    Adjoint,
    Leader;

    public boolean canWithdrawMoney() {
        return this.ordinal() >= Adjoint.ordinal();
    }

    public boolean canClaim() {
        return this.ordinal() >= Adjoint.ordinal();
    }

    public boolean canSetSpawn() {
        return this.ordinal() >= Adjoint.ordinal();
    }

    public boolean canInvite() {
        return this.ordinal() >= Adjoint.ordinal();
    }
}

