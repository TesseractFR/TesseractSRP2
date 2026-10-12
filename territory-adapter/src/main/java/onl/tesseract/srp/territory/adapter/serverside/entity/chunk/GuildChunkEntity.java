package onl.tesseract.srp.territory.adapter.serverside.entity.chunk;

import jakarta.persistence.*;
import onl.tesseract.srp.territory.adapter.serverside.entity.GuildEntity;
import onl.tesseract.srp.territory.domain.model.TerritoryChunk;
import onl.tesseract.srp.territory.domain.model.guild.GuildChunk;

/**
 * JPA entity for GuildChunk domain object.
 */
@Entity
@DiscriminatorValue("GUILD")
public class GuildChunkEntity extends TerritoryChunkEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "guild_id")
    private GuildEntity guild;

    public GuildChunkEntity() {}

    public GuildChunkEntity(ChunkEntityId chunkEntityId, GuildEntity guild) {
        this.chunkEntityId = chunkEntityId;
        this.guild = guild;
    }

    @Override
    public GuildChunk toDomain() {
        return new  GuildChunk(chunkEntityId.toDomain(), guild.toDomain());
    }
}

