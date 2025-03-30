package onl.tesseract.srp.controller.event.chat

import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.core.persistence.hibernate.boutique.TPlayerInfoService
import onl.tesseract.core.title.TitleService
import onl.tesseract.lib.player.Gender
import onl.tesseract.lib.util.plus
import onl.tesseract.lib.util.toComponent
import onl.tesseract.srp.service.player.SrpPlayerService
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.springframework.stereotype.Component as SpringComponent

/**
 * Listen to chat events to append the player's prefix before his message
 */
@SpringComponent
class PlayerChatPrefixListener(private val renderer: ChatRenderer) : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onChat(event: AsyncChatEvent) {
        event.renderer(renderer)
    }
}

@SpringComponent
class SrpChatRenderer(
    private val playerService: SrpPlayerService,
    private val playerInfoService: TPlayerInfoService,
    private val titleService: TitleService,
) : ChatRenderer {

    override fun render(
        source: Player,
        sourceDisplayName: Component,
        message: Component,
        viewer: Audience
    ): Component {

        val srpPlayer = playerService.getPlayer(source.uniqueId)
        val playerInfo = playerInfoService.get(source.uniqueId)
        val title = titleService.getById(srpPlayer.titleID)

        val displayTitle = when(playerInfo.genre) {
            Gender.FEMALE -> title.nameF
            else -> title.nameM
        }

        val renderedMessage = Component.empty()
            .append(Component.text(displayTitle)
                .color(NamedTextColor.YELLOW)
                .append(" ".toComponent())
                .append(source.displayName()))
            .append(NamedTextColor.GRAY + " : ")
            .color(NamedTextColor.WHITE)
            .append(message)
        return renderedMessage
    }
}