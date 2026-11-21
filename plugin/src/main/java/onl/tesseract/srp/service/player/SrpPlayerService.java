package onl.tesseract.srp.service.player;

import onl.tesseract.srp.common.adapter.DomainEventPublisher;
import onl.tesseract.srp.domain.money.ledger.TransactionSubType;
import onl.tesseract.srp.domain.money.ledger.TransactionType;
import onl.tesseract.srp.common.domain.model.enums.PlayerRank;
import onl.tesseract.srp.common.domain.model.SrpPlayer;
import onl.tesseract.srp.common.domain.model.event.PlayerRankUpEvent;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.port.userside.PlayerIlluminationService;
import onl.tesseract.srp.common.domain.port.userside.PlayerMoneyService;
import onl.tesseract.srp.common.domain.port.userside.SrpPlayerProfileService;
import onl.tesseract.srp.common.domain.port.userside.PlayerRankService;
import onl.tesseract.srp.service.money.MoneyLedgerService;
import onl.tesseract.srp.service.money.TransferService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SrpPlayerService {
    private final SrpPlayerProfileService srpPlayerProfileService;
    private final PlayerRankService playerRankService;
    private final PlayerMoneyService playerMoneyService;
    private final PlayerIlluminationService playerIlluminationService;
    private final MoneyLedgerService ledgerService;
    private final DomainEventPublisher eventService;

    public SrpPlayerService(SrpPlayerProfileService srpPlayerProfileService,
                            PlayerRankService playerRankService,
                            PlayerMoneyService playerMoneyService,
                            PlayerIlluminationService playerIlluminationService,
                            MoneyLedgerService ledgerService,
                            DomainEventPublisher eventService) {
        this.srpPlayerProfileService = srpPlayerProfileService;
        this.playerRankService = playerRankService;
        this.playerMoneyService = playerMoneyService;
        this.playerIlluminationService = playerIlluminationService;
        this.ledgerService = ledgerService;
        this.eventService = eventService;
    }

    public SrpPlayer getPlayer(UUID id) {
        return srpPlayerProfileService.getPlayer(new PlayerId(id));
    }

    /**
     * Set the rank of the player.
     * @return True if the rank was updated, false if the player already had this rank.
     */
    public boolean setRank(UUID playerID, PlayerRank rank) {
        SrpPlayer player = getPlayer(playerID);
        if (!playerRankService.changeRank(new PlayerId(playerID), rank)) return false;
        eventService.publish(new PlayerRankUpEvent(playerID, rank));
        return true;
    }

    /**
     * Try to buy the next rank. Withdraw money and update the player title.
     * @return True if the rank was bought
     */
    @Transactional
    public boolean buyNextRank(UUID playerID) {
        SrpPlayer player = getPlayer(playerID);
        boolean result = playerRankService.buyNextRank(new PlayerId(playerID));
        if (result) {
            ledgerService.recordTransaction(
                ledgerService.getPlayerLedger(playerID),
                ledgerService.getServerLedger(),
                player.getRank().getCost(),
                TransactionType.Player,
                TransactionSubType.Player.Rank,
                player.getRank().name()
            );
            eventService.publish(new PlayerRankUpEvent(playerID, player.getRank()));
        }
        return result;
    }

    /**
     * Add money to a player's account
     * @return True if the transaction is successful
     */
    @Transactional
    public boolean giveMoneyAsStaff(UUID playerID, int amount) {
        SrpPlayer player = getPlayer(playerID);
        if (!playerMoneyService.adjustMoney(new PlayerId(playerID), amount)) return false;
        ledgerService.recordTransaction(
            ledgerService.getServerLedger(),
            ledgerService.getPlayerLedger(playerID),
            amount,
            TransactionType.Staff,
            TransactionSubType.Staff.Give
        );
        return true;
    }

    @Transactional
    public void takeMoney(UUID playerID, int amount, TransactionType type, TransactionSubType subType, String details) {
        SrpPlayer player = getPlayer(playerID);
        if ((long) player.getMoney() - amount < 0) {
            throw new IllegalArgumentException("Player " + playerID + " does not have enough money (current = " + player.getMoney() + ", to pay = " + amount + ")");
        }
        player.addMoney(-amount);
        ledgerService.recordTransaction(
            ledgerService.getPlayerLedger(playerID),
            ledgerService.getServerLedger(),
            -amount,
            type,
            subType,
            details
        );
        srpPlayerProfileService.savePlayer(player);
    }

    public void moneyTransaction(UUID playerID, int amount, TransferService.TransferTransactionBuilder transactionBuilder) {
        SrpPlayer player = getPlayer(playerID);
        if (player.getMoney() + amount < 0) {
            throw new IllegalArgumentException("Player " + playerID + " does not have enough money (current = " + player.getMoney() + ", to pay = " + amount + ")");
        }
        if (amount < 0) {
            transactionBuilder.setFrom(ledgerService.getPlayerLedger(playerID));
        } else {
            transactionBuilder.setTo(ledgerService.getPlayerLedger(playerID));
        }
        player.addMoney(amount);
        srpPlayerProfileService.savePlayer(player);
    }

    @Transactional
    public boolean giveIlluminationPoints(UUID playerID, int amount) {
        SrpPlayer player = getPlayer(playerID);
        return playerIlluminationService.adjustIlluminationPoints(new PlayerId(playerID), amount);
    }

    protected void savePlayer(SrpPlayer player) {
        srpPlayerProfileService.savePlayer(player);
    }
}

