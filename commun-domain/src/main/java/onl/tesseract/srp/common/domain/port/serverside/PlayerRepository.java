package onl.tesseract.srp.common.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.SrpPlayer;

/** Persistence port for player data. */
public interface PlayerRepository {
    SrpPlayer getById(PlayerId id);

    SrpPlayer save(SrpPlayer player);
}
