package onl.tesseract.srp.controller.event.money

import onl.tesseract.srp.service.money.MoneyLedgerService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerLoadEvent
import org.springframework.stereotype.Component

@Component
class LedgerCreationListener(private val ledgerService: MoneyLedgerService) : Listener {

    @EventHandler
    fun onServerStarts(event: ServerLoadEvent) {
        ledgerService.createLedger(ledgerService.getServerLedger())
    }
}