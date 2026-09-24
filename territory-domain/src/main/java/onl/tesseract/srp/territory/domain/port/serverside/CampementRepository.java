package onl.tesseract.srp.territory.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.territory.domain.model.campement.Campement;

import java.util.List;
import java.util.UUID;

public interface CampementRepository extends TerritoryRepository<Campement, UUID> {
    void deleteById(UUID id);

    boolean isChunkClaimed(ChunkCoord chunkCoord);

    Campement getCampementByChunk(ChunkCoord chunkCoord);

    List<Campement> findAll();
}

