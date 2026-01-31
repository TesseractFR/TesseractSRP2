package onl.tesseract.srp.service.player

import onl.tesseract.srp.DomainEventPublisher
import onl.tesseract.srp.domain.player.event.PlayerRankUpEvent
import onl.tesseract.srp.domain.money.ledger.TransactionSubType
import onl.tesseract.srp.domain.money.ledger.TransactionType
import onl.tesseract.srp.domain.player.PlayerRank
import onl.tesseract.srp.domain.player.SrpPlayer
import onl.tesseract.srp.repository.generic.player.SrpPlayerRepository
import onl.tesseract.srp.service.money.MoneyLedgerService
import onl.tesseract.srp.service.money.TransferService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
open class SrpPlayerService(
    private val repository: SrpPlayerRepository,
    private val ledgerService: MoneyLedgerService,
    private val eventService: DomainEventPublisher
) {

    open fun getPlayer(id: UUID): SrpPlayer {
        return repository.getById(id) ?: SrpPlayer(id)
    }

    /**
     * Set the rank of the player.
     * @return True if the rank was updated, false if the player already had this rank.
     */
    open fun setRank(playerID: UUID, rank: PlayerRank): Boolean {
        val player = getPlayer(playerID)
        if (player.rank == rank) return false
        player.rank = rank
        player.titleID = rank.title.id
        savePlayer(player)
        eventService.publish(PlayerRankUpEvent(playerID, rank))
        return true
    }

    /**
     * Try to buy the next rank. Withdraw money and update the player title.
     * @return True if the rank was bought
     */
    @Transactional
    open fun buyNextRank(playerID: UUID): Boolean {
        val player = getPlayer(playerID)
        val result = player.buyNextRank()
        if (result) {
            player.titleID = player.rank.title.id
            ledgerService.recordTransaction(
                from = ledgerService.getPlayerLedger(playerID),
                to = ledgerService.getServerLedger(),
                amount = player.rank.cost,
                TransactionType.Player,
                TransactionSubType.Player.Rank,
                player.rank.name
            )
            savePlayer(player)
            eventService.publish(PlayerRankUpEvent(playerID, player.rank))
        }
        return result
    }

    /**
     * Add money to a player's account
     * @return True if the transaction is successful
     */
    @Transactional
    open fun giveMoneyAsStaff(playerID: UUID, amount: Int): Boolean {
        val player = getPlayer(playerID)
        if (player.money + amount < 0)
            return false
        player.addMoney(amount)
        ledgerService.recordTransaction(
            from = ledgerService.getServerLedger(),
            to = ledgerService.getPlayerLedger(playerID),
            amount = amount,
            TransactionType.Staff,
            TransactionSubType.Staff.Give,
        )
        savePlayer(player)
        return true
    }

    @Transactional
    open fun takeMoney(
        playerID: UUID,
        amount: Int,
        type: TransactionType,
        subType: TransactionSubType?,
        details: String?
    ) {
        val player = getPlayer(playerID)
        require(player.money - amount >= 0) {
            "Player $playerID does not have enough money (current = ${player.money}, to pay = $amount)"
        }
        player.addMoney(-amount)
        ledgerService.recordTransaction(
            from = ledgerService.getPlayerLedger(playerID),
            to = ledgerService.getServerLedger(),
            amount = -amount,
            type,
            subType,
            details
        )
        savePlayer(player)
    }

    open fun moneyTransaction(
        playerID: UUID,
        amount: Int,
        transactionBuilder: TransferService.TransferTransactionBuilder
    ) {
        val player = getPlayer(playerID)
        require(player.money + amount >= 0) {
            "Player $playerID does not have enough money (current = ${player.money}, to pay = ${amount})"
        }
        if (amount < 0)
            transactionBuilder.from = ledgerService.getPlayerLedger(playerID)
        else
            transactionBuilder.to = ledgerService.getPlayerLedger(playerID)
        player.addMoney(amount)
        savePlayer(player)
    }

    protected open fun savePlayer(player: SrpPlayer) {
        repository.save(player)
    }
}
