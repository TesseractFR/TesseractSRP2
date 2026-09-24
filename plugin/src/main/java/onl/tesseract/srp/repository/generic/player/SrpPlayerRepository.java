package onl.tesseract.srp.repository.generic.player;

import onl.tesseract.srp.domain.player.SrpPlayer;
import java.util.UUID;

public interface SrpPlayerRepository {
    SrpPlayer getById(UUID id);
    SrpPlayer save(SrpPlayer entity);
}

