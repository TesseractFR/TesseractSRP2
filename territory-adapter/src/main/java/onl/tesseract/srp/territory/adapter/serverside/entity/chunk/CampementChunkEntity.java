package onl.tesseract.srp.territory.adapter.serverside.entity.chunk;

import jakarta.persistence.*;
import onl.tesseract.srp.territory.adapter.serverside.entity.CampementEntity;
import onl.tesseract.srp.territory.domain.model.TerritoryChunk;
import onl.tesseract.srp.territory.domain.model.campement.CampementChunk;

@Entity
@DiscriminatorValue("CAMPEMENT")
public class CampementChunkEntity extends TerritoryChunkEntity{
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "campement_id")
    private CampementEntity campement;

    public CampementChunkEntity() {}

    public CampementChunkEntity(ChunkEntityId chunkEntityId, CampementEntity campement) {
        this.chunkEntityId = chunkEntityId;
        this.campement = campement;
    }

    @Override
    public TerritoryChunk toDomain() {
        return new CampementChunk(chunkEntityId.toDomain(),campement.toDomain());
    }
}
