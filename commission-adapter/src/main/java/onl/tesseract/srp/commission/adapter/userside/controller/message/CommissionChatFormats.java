package onl.tesseract.srp.commission.adapter.userside.controller.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/** Préfixes des messages de commission dans le chat. */
public final class CommissionChatFormats {
    /** Préfixe des messages d'information, en gris : les valeurs importantes sont mises en couleur dans le message. */
    public static final Component COMMISSION_CHAT_FORMAT = Component.empty().color(NamedTextColor.GRAY)
            .append(Component.empty()
                    .append(Component.text("[", NamedTextColor.GOLD, TextDecoration.BOLD))
                    .append(Component.text("Commission", NamedTextColor.YELLOW))
                    .append(Component.text("] ", NamedTextColor.GOLD, TextDecoration.BOLD)));
    public static final Component COMMISSION_CHAT_ERROR = COMMISSION_CHAT_FORMAT.color(NamedTextColor.RED);
    public static final Component COMMISSION_CHAT_SUCCESS = COMMISSION_CHAT_FORMAT.color(NamedTextColor.GREEN);

    private CommissionChatFormats() {
    }
}
