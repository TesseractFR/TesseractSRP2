package onl.tesseract.srp.territory.domain.model.campement;

import lombok.Getter;
import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.territory.domain.model.TerritoryChunk;

@Getter
public class CampementChunk extends TerritoryChunk {
    private final Campement campement;

    public CampementChunk(ChunkCoord chunkCoord, Campement campement) {
        super(chunkCoord);
        this.campement = campement;
    }

    @Override
    public Campement getOwner() {
        return campement;
    }
}

