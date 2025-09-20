package onl.tesseract.srp.controller.event.shop.listener

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import onl.tesseract.srp.service.shop.ShopService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.springframework.stereotype.Component

@Component
class ShopPriceInputListener(
    private val shopService: ShopService
) : Listener {

    @EventHandler
    fun onPlayerChat(event: AsyncChatEvent) {
        val player = event.player
        val message = PlainTextComponentSerializer.plainText().serialize(event.message())
        
        // On traite l'entrée
        val processed = shopService.processPriceInput(player, message)

        // S'il est bien en création on cancel le msg
        if (processed) {
            event.isCancelled = true
        }
    }
}