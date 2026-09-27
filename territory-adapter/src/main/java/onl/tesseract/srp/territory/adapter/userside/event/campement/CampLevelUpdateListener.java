package onl.tesseract.srp.territory.adapter.userside.event.campement;


import onl.tesseract.srp.common.domain.model.event.PlayerRankUpEvent;
import onl.tesseract.srp.territory.domain.port.userside.campement.CampementService;
import org.bukkit.event.Listener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CampLevelUpdateListener implements Listener {

    private final CampementService campementService;

    public CampLevelUpdateListener(CampementService campementService) {
        this.campementService = campementService;
    }

    @EventListener
    public void onPlayerRankUpdate(PlayerRankUpEvent event) {
        campementService.setCampLevel(event.getPlayerId(), event.getNewRank().getCampLevel());
    }
}
