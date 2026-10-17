package onl.tesseract.srp.territory.domain.model.guild.enums;

import net.kyori.adventure.text.format.NamedTextColor;

@SuppressWarnings("java:S115")
public enum GuildRole {
    Citoyen("Citoyen", NamedTextColor.GRAY),
    Batisseur("Batisseur", NamedTextColor.GREEN),
    Adjoint("Adjoint", NamedTextColor.GOLD),
    Leader("Leader", NamedTextColor.RED);

    private final String displayName;
    private final NamedTextColor color;

    GuildRole(String displayName, NamedTextColor color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public NamedTextColor getColor() { return color; }

    public boolean canWithdrawMoney() { return compareTo(Adjoint) >= 0; }
    public boolean canClaim()         { return compareTo(Adjoint) >= 0; }
    public boolean canSetSpawn()      { return compareTo(Adjoint) >= 0; }
    public boolean canInvite()        { return compareTo(Adjoint) >= 0; }
    public boolean canBuild()         { return compareTo(Batisseur) >= 0; }
}