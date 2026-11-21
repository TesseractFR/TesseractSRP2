package onl.tesseract.srp.common.domain.port.userside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.SrpPlayer;
import onl.tesseract.srp.common.domain.model.enums.PlayerRank;

import java.util.Objects;

/** Progression du joueur dans les grades SRP. */
public class PlayerRankService {

    private final SrpPlayerProfileService srpPlayerProfileService;

    public PlayerRankService(SrpPlayerProfileService srpPlayerProfileService) {
        this.srpPlayerProfileService = Objects.requireNonNull(srpPlayerProfileService);
    }

    /**
     * Donne directement un grade au joueur, avec le titre correspondant.
     *
     * @return {@code false} si le joueur a déjà ce grade, rien n'est alors modifié
     */
    public boolean changeRank(PlayerId playerId, PlayerRank rank) {
        SrpPlayer player = srpPlayerProfileService.getPlayer(playerId);
        if (player.getRank() == rank) return false;
        player.setRank(rank);
        synchronizeTitle(player);
        srpPlayerProfileService.savePlayer(player);
        return true;
    }

    /**
     * Achète le grade suivant en débitant son coût du solde du joueur, et met à jour son titre.
     *
     * @return {@code false} si le joueur a déjà le dernier grade ou n'a pas assez d'argent
     */
    public boolean buyNextRank(PlayerId playerId) {
        SrpPlayer player = srpPlayerProfileService.getPlayer(playerId);
        if (!player.buyNextRank()) return false;
        synchronizeTitle(player);
        srpPlayerProfileService.savePlayer(player);
        return true;
    }

    /** Le titre affiché suit toujours le grade. */
    private void synchronizeTitle(SrpPlayer player) {
        player.setTitleID(player.getRank().getTitle().getId());
    }
}
