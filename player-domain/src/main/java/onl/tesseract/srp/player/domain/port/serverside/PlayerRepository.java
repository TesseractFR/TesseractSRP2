package onl.tesseract.srp.player.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.player.domain.model.SrpPlayer;

import java.util.Optional;

/** Persistance du profil SRP des joueurs. */
public interface PlayerRepository {

    /** Renvoie le profil enregistré du joueur, ou vide s'il n'a encore rien enregistré. */
    Optional<SrpPlayer> findById(PlayerId id);

    SrpPlayer save(SrpPlayer player);
}
