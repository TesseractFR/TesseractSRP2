package onl.tesseract.srp.player.domain.port.userside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.player.domain.model.SrpPlayer;
import onl.tesseract.srp.player.domain.port.serverside.PlayerLedgerPort;

import java.util.Objects;

/** Gestion du solde d'argent d'un joueur. */
public class PlayerMoneyService {

    private final SrpPlayerProfileService srpPlayerProfileService;
    private final PlayerLedgerPort playerLedgerPort;

    public PlayerMoneyService(SrpPlayerProfileService srpPlayerProfileService, PlayerLedgerPort playerLedgerPort) {
        this.srpPlayerProfileService = Objects.requireNonNull(srpPlayerProfileService);
        this.playerLedgerPort = Objects.requireNonNull(playerLedgerPort);
    }

    /**
     * Ajoute (montant positif) ou retire (montant négatif) de l'argent au joueur.
     *
     * @return {@code false} si le solde deviendrait négatif, rien n'est alors modifié
     */
    public boolean adjustMoney(PlayerId playerId, int amount) {
        SrpPlayer player = srpPlayerProfileService.getPlayer(playerId);
        if ((long) player.getMoney() + amount < 0) return false;
        player.addMoney(amount);
        srpPlayerProfileService.savePlayer(player);
        return true;
    }

    /**
     * Ajustement de solde fait par le staff : même règle que {@link #adjustMoney}, et le mouvement est journalisé.
     *
     * @return {@code false} si le solde deviendrait négatif, rien n'est alors modifié ni journalisé
     */
    public boolean adjustMoneyAsStaff(PlayerId playerId, int amount) {
        if (!adjustMoney(playerId, amount)) return false;
        playerLedgerPort.recordStaffMoneyAdjustment(playerId, amount);
        return true;
    }
}
