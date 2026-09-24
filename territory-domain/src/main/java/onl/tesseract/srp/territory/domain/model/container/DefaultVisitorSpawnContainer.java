package onl.tesseract.srp.territory.domain.model.container;

import onl.tesseract.srp.common.domain.model.Coordinate;
import onl.tesseract.srp.territory.domain.model.enums.result.SetSpawnResult;

import java.util.UUID;

public class DefaultVisitorSpawnContainer implements VisitorSpawnContainer {
    protected Coordinate visitorSpawnPoint;

    public DefaultVisitorSpawnContainer(Coordinate visitorSpawnPoint) {
        this.visitorSpawnPoint = visitorSpawnPoint;
    }

    @Override
    public SetSpawnResult setVisitorSpawnpoint(Coordinate newLocation, UUID player) {
        this.visitorSpawnPoint = newLocation;
        return SetSpawnResult.SUCCESS;
    }

    @Override
    public boolean canSetSpawn(UUID player) {
        throw new UnsupportedOperationException("Must be defined");
    }

    @Override
    public Coordinate getVisitorSpawnpoint() {
        return visitorSpawnPoint;
    }
}

