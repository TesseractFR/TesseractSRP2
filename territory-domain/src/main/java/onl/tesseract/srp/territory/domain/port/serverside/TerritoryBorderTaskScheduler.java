package onl.tesseract.srp.territory.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.ChunkCoord;

import java.util.List;
import java.util.UUID;

public interface TerritoryBorderTaskScheduler {
    void schedule(UUID player, List<ChunkCoord> chunkCoords);

    void cancel(UUID player);

    boolean isActive(UUID player);
}
