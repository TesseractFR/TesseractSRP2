package onl.tesseract.srp.territory.domain.port.userside.campement;

import onl.tesseract.srp.territory.domain.model.campement.Campement;
import onl.tesseract.srp.territory.domain.model.campement.CampementChunk;
import onl.tesseract.srp.territory.domain.port.serverside.PlayerCampementMessagingRepository;
import onl.tesseract.srp.territory.domain.port.serverside.TerritoryBorderTaskScheduler;
import onl.tesseract.srp.territory.domain.port.userside.TerritoryBorderService;

import java.util.UUID;

public class CampementBorderService extends TerritoryBorderService<CampementChunk, Campement> {

    private final PlayerCampementMessagingRepository messaging;

    public CampementBorderService(
            TerritoryBorderTaskScheduler scheduler,
            CampementService territoryService,
            PlayerCampementMessagingRepository messaging) {
        super(scheduler, territoryService);
        this.messaging = messaging;
    }

    public void toggle(UUID player, String world) {
        switch (toggleBorders(player, world)) {
            case SHOW_BORDERS -> messaging.playerBordersShown(player);
            case CLEAR_BORDERS -> messaging.playerBordersHidden(player);
            case TERRITORY_NOT_FOUND -> messaging.playerNoCampement(player);
            case INVALID_WORLD -> messaging.playerInvalidWorld(player);
        }
    }
}

