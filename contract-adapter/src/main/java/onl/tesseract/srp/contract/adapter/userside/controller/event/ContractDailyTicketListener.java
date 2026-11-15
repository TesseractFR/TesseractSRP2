package onl.tesseract.srp.contract.adapter.userside.controller.event;

import onl.tesseract.core.dailyConnection.event.PlayerServerDailyConnectionEvent;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.port.userside.ContractTicketUseCase;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.springframework.stereotype.Component;

/** Renouvelle le solde de tickets lorsqu'un joueur se connecte pour une nouvelle journée serveur. */
@Component
public class ContractDailyTicketListener implements Listener {

    private final ContractTicketUseCase contractTicketUseCase;

    public ContractDailyTicketListener(ContractTicketUseCase contractTicketUseCase) {
        this.contractTicketUseCase = contractTicketUseCase;
    }

    @EventHandler
    public void onDailyConnection(PlayerServerDailyConnectionEvent event) {
        contractTicketUseCase.renewDaily(new PlayerId(event.getPlayerID()));
    }
}
