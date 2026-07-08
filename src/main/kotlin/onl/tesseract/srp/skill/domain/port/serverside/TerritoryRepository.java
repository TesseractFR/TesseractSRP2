package onl.tesseract.srp.skill.domain.port.serverside;

import onl.tesseract.srp.domain.commun.ChunkCoord;

import java.util.UUID;

public interface TerritoryRepository {
    UUID get(ChunkCoord coord);
}
