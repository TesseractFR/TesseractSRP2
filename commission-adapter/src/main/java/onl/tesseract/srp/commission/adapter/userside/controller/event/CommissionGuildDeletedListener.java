package onl.tesseract.srp.commission.adapter.userside.controller.event;

import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.commission.domain.port.userside.CommissionBoardService;
import onl.tesseract.srp.territory.domain.model.guild.event.GuildDeletedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Supprime le tableau d'une guilde, dans le monde et en base, avec ses commissions quand la guilde est supprimée. */
@Component
public class CommissionGuildDeletedListener {

    private final CommissionBoardService commissionBoardService;

    public CommissionGuildDeletedListener(CommissionBoardService commissionBoardService) {
        this.commissionBoardService = commissionBoardService;
    }

    @EventListener
    public void onGuildDeleted(GuildDeletedEvent event) {
        commissionBoardService.deleteGuildBoard(new GuildId(event.guildId()));
    }
}
