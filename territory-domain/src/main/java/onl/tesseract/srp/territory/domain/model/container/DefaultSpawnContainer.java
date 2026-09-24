package onl.tesseract.srp.territory.domain.model.container;

import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.common.domain.model.Coordinate;
import onl.tesseract.srp.territory.domain.model.enums.result.SetSpawnResult;

import java.util.UUID;

public class DefaultSpawnContainer implements SpawnContainer {
    private Coordinate spawnpoint;

    public DefaultSpawnContainer(Coordinate spawnpoint) {
        this.spawnpoint = spawnpoint;
    }

    @Override
    public SetSpawnResult setSpawnpoint(Coordinate coordinate, UUID player) {
        this.spawnpoint = coordinate;
        return SetSpawnResult.SUCCESS;
    }

    @Override
    public boolean isSpawnChunk(ChunkCoord chunkCoord) {
        return spawnpoint.chunkCoord().equals(chunkCoord);
    }

    @Override
    public boolean canSetSpawn(UUID player) {
        throw new IllegalAccessError("La méthode doit être override");
    }

    @Override
    public Coordinate getSpawnpoint() {
        return spawnpoint;
    }
}

