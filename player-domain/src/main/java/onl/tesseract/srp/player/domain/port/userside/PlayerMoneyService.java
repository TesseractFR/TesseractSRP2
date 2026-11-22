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
     * Ajoute de l'argent au joueur. Le dépassement de la valeur maximale n'est pas contrôlé.
     *
     * @throws IllegalArgumentException si le montant est négatif
     */
    public void addMoney(PlayerId playerId, int amount) {
        requireNotNegative(amount);
        adjustMoney(playerId, amount);
    }

    /**
     * Retire de l'argent au joueur.
     *
     * @return {@code false} si le joueur n'a pas assez d'argent, rien n'est alors modifié
     * @throws IllegalArgumentException si le montant est négatif
     */
    public boolean removeMoney(PlayerId playerId, int amount) {
        requireNotNegative(amount);
        return adjustMoney(playerId, -amount);
    }

    /**
     * Don d'argent fait par le staff : même règle que {@link #addMoney}, et le mouvement est journalisé.
     *
     * @throws IllegalArgumentException si le montant est négatif
     */
    public void addMoneyAsStaff(PlayerId playerId, int amount) {
        addMoney(playerId, amount);
        playerLedgerPort.recordStaffMoneyAdjustment(playerId, amount);
    }

    /**
     * Retrait d'argent fait par le staff : même règle que {@link #removeMoney}, et le mouvement est journalisé.
     *
     * @return {@code false} si le joueur n'a pas assez d'argent, rien n'est alors modifié ni journalisé
     * @throws IllegalArgumentException si le montant est négatif
     */
    public boolean removeMoneyAsStaff(PlayerId playerId, int amount) {
        if (!removeMoney(playerId, amount)) return false;
        playerLedgerPort.recordStaffMoneyAdjustment(playerId, -amount);
        return true;
    }

    /** Applique la variation, sauf si elle ferait passer le solde sous zéro. */
    private boolean adjustMoney(PlayerId playerId, int variation) {
        SrpPlayer player = srpPlayerProfileService.getPlayer(playerId);
        if (variation < 0 && player.getMoney() < -variation) return false;
        player.addMoney(variation);
        srpPlayerProfileService.savePlayer(player);
        return true;
    }

    private static void requireNotNegative(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Le montant ne peut pas être négatif : " + amount);
        }
    }
}
