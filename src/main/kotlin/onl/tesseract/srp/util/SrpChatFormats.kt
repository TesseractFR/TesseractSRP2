package onl.tesseract.srp.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import onl.tesseract.lib.util.append


val jobsChatFormat: Component = empty().color(NamedTextColor.GRAY)
    .append(
        empty()
            .append("[", NamedTextColor.GOLD, TextDecoration.BOLD)
            .append("Métier", NamedTextColor.YELLOW)
            .append("] ", NamedTextColor.GOLD, TextDecoration.BOLD)
    )
val jobsChatFormatError: Component = jobsChatFormat.color(NamedTextColor.RED)
val jobsChatFormatSuccess: Component = jobsChatFormat.color(NamedTextColor.GREEN)

val CampementChatFormat: Component = empty().color(NamedTextColor.GRAY)
    .append(
        empty()
            .append("[", NamedTextColor.DARK_AQUA, TextDecoration.BOLD)
            .append("Campement", NamedTextColor.AQUA)
            .append("] ", NamedTextColor.DARK_AQUA, TextDecoration.BOLD)
    )
val CampementChatError: Component = CampementChatFormat.color(NamedTextColor.RED)
val CampementChatSuccess: Component = CampementChatFormat.color(NamedTextColor.GREEN)

val GuildChatFormat: Component = empty().color(NamedTextColor.GRAY)
    .append(
        empty()
            .append("[", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD)
            .append("Guilde", NamedTextColor.LIGHT_PURPLE)
            .append("] ", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD)
    )
val GuildChatError: Component = GuildChatFormat.color(NamedTextColor.RED)
val GuildChatSuccess: Component = GuildChatFormat.color(NamedTextColor.GREEN)

val StaffChatFormat: Component = empty().color(NamedTextColor.GRAY)
    .append(
        empty()
            .append("[", NamedTextColor.DARK_RED, TextDecoration.BOLD)
            .append("Staff", NamedTextColor.RED)
            .append("] ", NamedTextColor.DARK_RED, TextDecoration.BOLD)
    )
val StaffChatError: Component = StaffChatFormat.color(NamedTextColor.RED)
val StaffChatSuccess: Component = StaffChatFormat.color(NamedTextColor.GREEN)