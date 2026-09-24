package onl.tesseract.srp.territory.domain.model.container;

import onl.tesseract.srp.common.domain.model.Coordinate;
import onl.tesseract.srp.territory.domain.model.enums.result.SetSpawnResult;

import java.util.UUID;

public interface VisitorSpawnContainer {
    SetSpawnResult setVisitorSpawnpoint(Coordinate newLocation, UUID player);
    boolean canSetSpawn(UUID player);
    Coordinate getVisitorSpawnpoint();
}

