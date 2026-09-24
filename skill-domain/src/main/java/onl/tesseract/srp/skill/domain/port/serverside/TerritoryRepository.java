package onl.tesseract.srp.skill.domain.port.serverside;


import onl.tesseract.srp.common.domain.model.ChunkCoord;

import java.util.UUID;

public interface TerritoryRepository {
    UUID get(ChunkCoord coord);
}
