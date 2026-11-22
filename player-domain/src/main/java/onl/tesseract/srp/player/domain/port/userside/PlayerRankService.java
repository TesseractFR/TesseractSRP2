package onl.tesseract.srp.player.domain.port.userside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.port.userside.plugin.EventPublisher;
import onl.tesseract.srp.player.domain.model.PlayerRank;
import onl.tesseract.srp.player.domain.model.SrpPlayer;
import onl.tesseract.srp.player.domain.model.event.PlayerRankUpEvent;
import onl.tesseract.srp.player.domain.port.serverside.PlayerLedgerPort;

import java.util.Objects;

/** Progression du joueur dans les grades SRP. */
public class PlayerRankService {

    private final SrpPlayerProfileService srpPlayerProfileService;
    private final PlayerLedgerPort playerLedgerPort;
    private final EventPublisher eventPublisher;

    public PlayerRankService(SrpPlayerProfileService srpPlayerProfileService,
                             PlayerLedgerPort playerLedgerPort,
                             EventPublisher eventPublisher) {
        this.srpPlayerProfileService = Objects.requireNonNull(srpPlayerProfileService);
        this.playerLedgerPort = Objects.requireNonNull(playerLedgerPort);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
    }

    /**
     * Donne directement un grade au joueur, avec le titre correspondant, et publie {@link PlayerRankUpEvent}.
     *
     * @return {@code false} si le joueur a déjà ce grade, rien n'est alors modifié
     */
    public boolean changeRank(PlayerId playerId, PlayerRank rank) {
        SrpPlayer player = srpPlayerProfileService.getPlayer(playerId);
        if (player.getRank() == rank) return false;
        player.setRank(rank);
        synchronizeTitle(player);
        srpPlayerProfileService.savePlayer(player);
        eventPublisher.publish(new PlayerRankUpEvent(playerId.value(), rank));
        return true;
    }

    /**
     * Achète le grade suivant en débitant son coût du solde du joueur, met à jour son titre,
     * journalise le paiement et publie {@link PlayerRankUpEvent} avec le nouveau grade.
     *
     * @return {@code false} si le joueur a déjà le dernier grade ou n'a pas assez d'argent
     */
    public boolean buyNextRank(PlayerId playerId) {
        SrpPlayer player = srpPlayerProfileService.getPlayer(playerId);
        if (!player.buyNextRank()) return false;
        synchronizeTitle(player);
        srpPlayerProfileService.savePlayer(player);
        PlayerRank purchasedRank = player.getRank();
        playerLedgerPort.recordRankPurchase(playerId, purchasedRank);
        eventPublisher.publish(new PlayerRankUpEvent(playerId.value(), purchasedRank));
        return true;
    }

    /** Le titre affiché suit toujours le grade. */
    private void synchronizeTitle(SrpPlayer player) {
        player.setTitleID(player.getRank().getTitle().getId());
    }
}
