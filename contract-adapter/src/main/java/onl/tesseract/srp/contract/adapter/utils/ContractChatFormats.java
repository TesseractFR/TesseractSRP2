package onl.tesseract.srp.contract.adapter.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class ContractChatFormats {
    private ContractChatFormats() {
    }

    public static final Component CONTRACT_CHAT_FORMAT = Component.empty().color(NamedTextColor.GRAY)
            .append(
                    Component.empty()
                            .append(Component.text("[", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
                            .append(Component.text("Contrat", NamedTextColor.LIGHT_PURPLE))
                            .append(Component.text("] ", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
            );
    public static final Component CONTRACT_CHAT_ERROR = CONTRACT_CHAT_FORMAT.color(NamedTextColor.RED);
    public static final Component CONTRACT_CHAT_SUCCESS = CONTRACT_CHAT_FORMAT.color(NamedTextColor.GREEN);
}