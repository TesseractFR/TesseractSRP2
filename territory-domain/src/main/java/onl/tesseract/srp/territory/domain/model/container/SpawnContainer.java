package onl.tesseract.srp.territory.domain.model.container;

import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.common.domain.model.Coordinate;
import onl.tesseract.srp.territory.domain.model.enums.result.SetSpawnResult;

import java.util.UUID;

public interface SpawnContainer {
    SetSpawnResult setSpawnpoint(Coordinate coordinate, UUID player);
    boolean isSpawnChunk(ChunkCoord chunkCoord);
    boolean canSetSpawn(UUID player);
    Coordinate getSpawnpoint();
}

