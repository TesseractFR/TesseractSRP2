package onl.tesseract.srp.territory.adapter.serverside.entity.chunk;

import jakarta.persistence.*;
import onl.tesseract.srp.territory.adapter.serverside.entity.CampementEntity;
import onl.tesseract.srp.territory.domain.model.TerritoryChunk;
import onl.tesseract.srp.territory.domain.model.campement.CampementChunk;

@Entity
@DiscriminatorValue("CAMPEMENT")
public class CampementChunkEntity extends TerritoryChunkEntity{
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "campement_id", nullable = false)
    private CampementEntity campement;

    @Override
    public TerritoryChunk toDomain() {
        return new CampementChunk(chunkEntityId.toDomain(),campement.toDomain());
    }
}
