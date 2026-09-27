package onl.tesseract.srp.territory.adapter.serverside.repository;

import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.territory.adapter.serverside.entity.chunk.CampementChunkEntity;
import onl.tesseract.srp.territory.adapter.serverside.entity.chunk.ChunkEntityId;
import onl.tesseract.srp.territory.adapter.serverside.entity.chunk.GuildChunkEntity;
import onl.tesseract.srp.territory.adapter.serverside.entity.chunk.TerritoryChunkEntity;
import onl.tesseract.srp.territory.domain.model.TerritoryChunk;
import onl.tesseract.srp.territory.domain.model.guild.GuildChunk;
import onl.tesseract.srp.territory.domain.port.serverside.TerritoryChunkRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class TerritoryChunkRepositoryImpl implements TerritoryChunkRepository {

    private final TerritoryChunkJpaRepository jpaRepository;

    public TerritoryChunkRepositoryImpl(TerritoryChunkJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public TerritoryChunk getById(ChunkCoord id) {
        TerritoryChunkEntity entity = jpaRepository.findById(ChunkEntityId.fromDomain(id)).orElse(null);
        return entity != null ? toDomain(entity) : null;
    }

    @Override
    public <T extends TerritoryChunk> T findByIdAndType(ChunkCoord id, Class<T> type) {
        TerritoryChunkEntity entity = jpaRepository.findById(ChunkEntityId.fromDomain(id)).orElse(null);
        if (entity == null) return null;
        if (!isType(entity, type)) return null;
        @SuppressWarnings("unchecked")
        T result = (T) toDomain(entity);
        return result;
    }

    @Override
    public Collection<TerritoryChunk> findAllByRange(String world, int minX, int maxX, int minZ, int maxZ) {
        return jpaRepository.findAllByRange(world, minX, maxX, minZ, maxZ).stream()
                .map(this::toDomain)
                .toList();
    }

    private TerritoryChunk toDomain(TerritoryChunkEntity entity) {
        return entity.toDomain();
    }

    private boolean isType(TerritoryChunkEntity entity, Class<?> type) {
        if (type == null) return true;
        if (type.equals(GuildChunk.class)) {
            return entity instanceof GuildChunkEntity;
        }
        if (type.equals(onl.tesseract.srp.territory.domain.model.campement.CampementChunk.class)) {
            return entity instanceof CampementChunkEntity;
        }
        return true;
    }

    @Override
    public TerritoryChunk save(TerritoryChunk territoryChunk) {
        return null;
    }

    @Override
    public ChunkCoord idOf(TerritoryChunk territoryChunk) {
        return territoryChunk.getChunkCoord();
    }
}

