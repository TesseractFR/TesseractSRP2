package onl.tesseract.srp.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Pre-defined chat formats for different game sections.
 */
public final class SrpChatFormats {
    private SrpChatFormats() {
        // Utility class
    }

    public static final Component JOBS_CHAT_FORMAT = Component.empty().color(NamedTextColor.GRAY)
            .append(
                    Component.empty()
                            .append(Component.text("[", NamedTextColor.GOLD, TextDecoration.BOLD))
                            .append(Component.text("Métier", NamedTextColor.YELLOW))
                            .append(Component.text("] ", NamedTextColor.GOLD, TextDecoration.BOLD))
            );
    public static final Component JOBS_CHAT_FORMAT_ERROR = JOBS_CHAT_FORMAT.color(NamedTextColor.RED);
    public static final Component JOBS_CHAT_FORMAT_SUCCESS = JOBS_CHAT_FORMAT.color(NamedTextColor.GREEN);

    public static final Component CAMPEMENT_CHAT_FORMAT = Component.empty().color(NamedTextColor.GRAY)
            .append(
                    Component.empty()
                            .append(Component.text("[", NamedTextColor.DARK_AQUA, TextDecoration.BOLD))
                            .append(Component.text("Campement", NamedTextColor.AQUA))
                            .append(Component.text("] ", NamedTextColor.DARK_AQUA, TextDecoration.BOLD))
            );
    public static final Component CAMPEMENT_CHAT_ERROR = CAMPEMENT_CHAT_FORMAT.color(NamedTextColor.RED);
    public static final Component CAMPEMENT_CHAT_SUCCESS = CAMPEMENT_CHAT_FORMAT.color(NamedTextColor.GREEN);

    public static final Component GUILD_CHAT_FORMAT = Component.empty().color(NamedTextColor.GRAY)
            .append(
                    Component.empty()
                            .append(Component.text("[", NamedTextColor.DARK_GREEN, TextDecoration.BOLD))
                            .append(Component.text("Guilde", NamedTextColor.GREEN))
                            .append(Component.text("] ", NamedTextColor.DARK_GREEN, TextDecoration.BOLD))
            );
    public static final Component GUILD_CHAT_ERROR = GUILD_CHAT_FORMAT.color(NamedTextColor.RED);
    public static final Component GUILD_CHAT_SUCCESS = GUILD_CHAT_FORMAT.color(NamedTextColor.GREEN);

    public static final Component STAFF_CHAT_FORMAT = Component.empty().color(NamedTextColor.GRAY)
            .append(
                    Component.empty()
                            .append(Component.text("[", NamedTextColor.DARK_RED, TextDecoration.BOLD))
                            .append(Component.text("Staff", NamedTextColor.RED))
                            .append(Component.text("] ", NamedTextColor.DARK_RED, TextDecoration.BOLD))
            );
    public static final Component STAFF_CHAT_ERROR = STAFF_CHAT_FORMAT.color(NamedTextColor.RED);
    public static final Component STAFF_CHAT_SUCCESS = STAFF_CHAT_FORMAT.color(NamedTextColor.GREEN);
}

