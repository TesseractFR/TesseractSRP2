package onl.tesseract.srp.service.player;

import onl.tesseract.srp.common.adapter.DomainEventPublisher;
import onl.tesseract.srp.domain.money.ledger.TransactionSubType;
import onl.tesseract.srp.domain.money.ledger.TransactionType;
import onl.tesseract.srp.domain.player.PlayerRank;
import onl.tesseract.srp.domain.player.SrpPlayer;
import onl.tesseract.srp.domain.player.event.PlayerRankUpEvent;
import onl.tesseract.srp.repository.generic.player.SrpPlayerRepository;
import onl.tesseract.srp.service.money.MoneyLedgerService;
import onl.tesseract.srp.service.money.TransferService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SrpPlayerService {
    private final SrpPlayerRepository repository;
    private final MoneyLedgerService ledgerService;
    private final DomainEventPublisher eventService;

    public SrpPlayerService(SrpPlayerRepository repository, MoneyLedgerService ledgerService, DomainEventPublisher eventService) {
        this.repository = repository;
        this.ledgerService = ledgerService;
        this.eventService = eventService;
    }

    public SrpPlayer getPlayer(UUID id) {
        SrpPlayer player = repository.getById(id);
        return player != null ? player : new SrpPlayer(id);
    }

    /**
     * Set the rank of the player.
     * @return True if the rank was updated, false if the player already had this rank.
     */
    public boolean setRank(UUID playerID, PlayerRank rank) {
        SrpPlayer player = getPlayer(playerID);
        if (player.getRank() == rank) return false;
        player.setRank(rank);
        player.setTitleID(rank.getTitle().getId());
        savePlayer(player);
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
        boolean result = player.buyNextRank();
        if (result) {
            player.setTitleID(player.getRank().getTitle().getId());
            ledgerService.recordTransaction(
                ledgerService.getPlayerLedger(playerID),
                ledgerService.getServerLedger(),
                player.getRank().getCost(),
                TransactionType.Player,
                TransactionSubType.Player.Rank,
                player.getRank().name()
            );
            savePlayer(player);
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
        if (player.getMoney() + amount < 0) return false;
        player.addMoney(amount);
        ledgerService.recordTransaction(
            ledgerService.getServerLedger(),
            ledgerService.getPlayerLedger(playerID),
            amount,
            TransactionType.Staff,
            TransactionSubType.Staff.Give
        );
        savePlayer(player);
        return true;
    }

    @Transactional
    public void takeMoney(UUID playerID, int amount, TransactionType type, TransactionSubType subType, String details) {
        SrpPlayer player = getPlayer(playerID);
        if (player.getMoney() - amount < 0) {
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
        savePlayer(player);
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
        savePlayer(player);
    }

    @Transactional
    public boolean giveIlluminationPoints(UUID playerID, int amount) {
        SrpPlayer player = getPlayer(playerID);
        if (player.getIlluminationPoints() + amount < 0) return false;
        player.addIlluminationPoints(amount);
        savePlayer(player);
        return true;
    }

    protected void savePlayer(SrpPlayer player) {
        repository.save(player);
    }
}

