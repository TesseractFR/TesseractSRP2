package onl.tesseract.srp.territory.domain.port.serverside;

import java.util.UUID;

public interface SrpPlayerRepository {
    boolean hasRequiredGuildRank(UUID playerId);

    int getCampLevel(UUID playerId);
}
