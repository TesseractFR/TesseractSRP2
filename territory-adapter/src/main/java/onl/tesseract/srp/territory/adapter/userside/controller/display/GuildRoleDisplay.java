package onl.tesseract.srp.territory.adapter.userside.controller.display;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRole;

@SuppressWarnings("java:S115")
@Getter
public enum GuildRoleDisplay {
    Citoyen("Citoyen", NamedTextColor.GRAY),
    Batisseur("Batisseur", NamedTextColor.GREEN),
    Adjoint("Adjoint", NamedTextColor.GOLD),
    Leader("Leader", NamedTextColor.RED);

    private final String displayName;
    private final NamedTextColor color;

    GuildRoleDisplay(String displayName, NamedTextColor color) {
        this.displayName = displayName;
        this.color = color;
    }

    public static GuildRoleDisplay of(GuildRole role) {
        return valueOf(role.name());
    }
}