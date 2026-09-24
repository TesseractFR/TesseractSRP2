package onl.tesseract.srp.territory.domain.port.serverside;

import onl.tesseract.lib.repository.Repository;
import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.territory.domain.model.TerritoryChunk;

import java.util.Collection;

public interface TerritoryChunkRepository extends Repository<TerritoryChunk, ChunkCoord> {
    <T extends TerritoryChunk> T findByIdAndType(ChunkCoord id, Class<T> type);

    Collection<TerritoryChunk> findAllByRange(String world, int minX, int maxX, int minZ, int maxZ);
}