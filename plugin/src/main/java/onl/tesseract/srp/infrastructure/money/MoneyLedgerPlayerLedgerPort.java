package onl.tesseract.srp.infrastructure.money;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.domain.money.ledger.TransactionSubType;
import onl.tesseract.srp.domain.money.ledger.TransactionType;
import onl.tesseract.srp.player.domain.model.PlayerRank;
import onl.tesseract.srp.player.domain.port.serverside.PlayerLedgerPort;
import onl.tesseract.srp.service.money.MoneyLedgerService;
import org.springframework.stereotype.Component;

/** Journalise les mouvements d'argent du joueur dans le journal des transactions du plugin. */
@Component
public class MoneyLedgerPlayerLedgerPort implements PlayerLedgerPort {

    private final MoneyLedgerService moneyLedgerService;

    public MoneyLedgerPlayerLedgerPort(MoneyLedgerService moneyLedgerService) {
        this.moneyLedgerService = moneyLedgerService;
    }

    @Override
    public void recordRankPurchase(PlayerId playerId, PlayerRank purchasedRank) {
        moneyLedgerService.recordTransaction(
                moneyLedgerService.getPlayerLedger(playerId.value()),
                moneyLedgerService.getServerLedger(),
                purchasedRank.getCost(),
                TransactionType.Player,
                TransactionSubType.Player.Rank,
                purchasedRank.name()
        );
    }

    @Override
    public void recordStaffMoneyAdjustment(PlayerId playerId, int amount) {
        moneyLedgerService.recordTransaction(
                moneyLedgerService.getServerLedger(),
                moneyLedgerService.getPlayerLedger(playerId.value()),
                amount,
                TransactionType.Staff,
                TransactionSubType.Staff.Give
        );
    }
}
