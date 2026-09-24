package onl.tesseract.srp.territory.adapter.serverside.entity.chunk;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import onl.tesseract.srp.territory.domain.model.TerritoryChunk;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
@Getter
@Setter
@NoArgsConstructor
public abstract class TerritoryChunkEntity {

    @EmbeddedId
    protected ChunkEntityId chunkEntityId;


    public abstract TerritoryChunk toDomain();
}

