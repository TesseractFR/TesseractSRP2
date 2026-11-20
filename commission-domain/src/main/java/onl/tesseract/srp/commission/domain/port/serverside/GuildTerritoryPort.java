package onl.tesseract.srp.commission.domain.port.serverside;

import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.common.domain.model.ChunkCoord;

/** Port d'accès au territoire des guildes. */
public interface GuildTerritoryPort {
    /** @return {@code true} si le chunk appartient à la guilde */
    boolean ownsChunk(GuildId guildId, ChunkCoord chunk);
}
