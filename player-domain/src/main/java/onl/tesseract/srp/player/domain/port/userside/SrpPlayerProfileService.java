package onl.tesseract.srp.player.domain.port.userside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.player.domain.model.SrpPlayer;
import onl.tesseract.srp.player.domain.port.serverside.PlayerRepository;

import java.util.Objects;

/** Lecture et sauvegarde du profil SRP d'un joueur. */
public class SrpPlayerProfileService {

    private final PlayerRepository repository;

    public SrpPlayerProfileService(PlayerRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    /**
     * Renvoie le profil enregistré du joueur.
     * Un joueur qui n'a encore rien enregistré reçoit un profil neuf avec les valeurs par défaut :
     * cette méthode ne renvoie donc jamais vide.
     */
    public SrpPlayer getPlayer(PlayerId playerId) {
        return repository.findById(playerId).orElseGet(() -> new SrpPlayer(playerId));
    }

    /** Enregistre le profil du joueur. */
    public void savePlayer(SrpPlayer player) {
        repository.save(player);
    }
}
