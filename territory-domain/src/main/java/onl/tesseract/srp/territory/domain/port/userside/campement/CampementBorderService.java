package onl.tesseract.srp.territory.domain.port.userside.campement;

import onl.tesseract.srp.territory.domain.model.campement.Campement;
import onl.tesseract.srp.territory.domain.model.campement.CampementChunk;
import onl.tesseract.srp.territory.domain.port.serverside.TerritoryBorderTaskScheduler;
import onl.tesseract.srp.territory.domain.port.userside.TerritoryBorderService;

public class CampementBorderService extends TerritoryBorderService<CampementChunk, Campement> {

    public CampementBorderService(
            TerritoryBorderTaskScheduler scheduler,
            CampementService territoryService) {
        super(scheduler, territoryService);
    }
}

