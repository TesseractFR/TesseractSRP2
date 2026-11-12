package onl.tesseract.srp.contract.adapter.userside.controller.event;

import onl.tesseract.core.dailyConnection.event.PlayerServerDailyConnectionEvent;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.port.userside.ContractService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.springframework.stereotype.Component;

/** Réinitialise le solde de tickets lorsqu'un joueur se connecte pour une nouvelle journée serveur. */
@Component
public class ContractDailyTicketListener implements Listener {

    private final ContractService contractService;

    public ContractDailyTicketListener(ContractService contractService) {
        this.contractService = contractService;
    }

    @EventHandler
    public void onDailyConnection(PlayerServerDailyConnectionEvent event) {
        contractService.renewDaily(new PlayerId(event.getPlayerID()));
    }
}
