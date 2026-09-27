package onl.tesseract.srp.territory.adapter.userside.event.campement;

import onl.tesseract.srp.territory.adapter.userside.event.territory.TerritoryBorderDisplayListener;
import onl.tesseract.srp.territory.domain.model.campement.Campement;
import onl.tesseract.srp.territory.domain.model.campement.CampementChunk;
import onl.tesseract.srp.territory.domain.model.campement.CampementChunkClaimEvent;
import onl.tesseract.srp.territory.domain.model.campement.CampementChunkUnclaimEvent;
import onl.tesseract.srp.territory.domain.port.userside.campement.CampementBorderService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CampementBorderDisplayListener
        extends TerritoryBorderDisplayListener<CampementChunk, Campement> {

    public CampementBorderDisplayListener(CampementBorderService campementBorderService) {
        super(campementBorderService);
    }

    @EventListener
    public void onChunkClaim(CampementChunkClaimEvent event) {
        updateBorders(event.getPlayerId());
    }

    @EventListener
    public void onChunkUnclaim(CampementChunkUnclaimEvent event) {
        updateBorders(event.getPlayerId());
    }
}
