package onl.tesseract.srp.territory.adapter.userside.controller.display;

import net.kyori.adventure.text.format.NamedTextColor;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRole;

public final class GuildRoleDisplay {
    private GuildRoleDisplay() {}

    public static String name(GuildRole role) {
        return role.name();
    }

    public static NamedTextColor color(GuildRole role) {
        return switch (role) {
            case Citoyen -> NamedTextColor.GRAY;
            case Batisseur -> NamedTextColor.GREEN;
            case Adjoint -> NamedTextColor.GOLD;
            case Leader -> NamedTextColor.RED;
        };
    }
}