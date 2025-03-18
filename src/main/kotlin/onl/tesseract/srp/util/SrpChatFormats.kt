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