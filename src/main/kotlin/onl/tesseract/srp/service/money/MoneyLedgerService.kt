package onl.tesseract.srp.service.money

import onl.tesseract.srp.domain.money.ledger.*
import onl.tesseract.srp.repository.hibernate.MoneyLedgerRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

/**
 * Record money transactions. All money given to a player or a guild must be registered here.
 */
@Service
class MoneyLedgerService(private val repository: MoneyLedgerRepository) {

    /**
     * Get or create a player ledger
     */
    fun getPlayerLedger(playerID: UUID): Ledger {
        return createLedger(Ledger(playerID, LedgerType.Player))
    }

    fun getServerLedger() = Ledger(UUID.fromString("00000000-0000-0000-0000-000000000000"), LedgerType.Server)

    fun createLedger(ledger: Ledger): Ledger {
        if (repository.getById(ledger.id) == null)
            repository.save(ledger)
        return ledger
    }

    /**
     * @param from The account giving the money. Use the server ledger with [getServerLedger] to create money (e.g for job rewards)
     * @param to The account receiving the money. Use the server ledger with [getServerLedger] to delete money (e.g to buy player ranks)
     * @param amount Amount of money to transfer. If negative, the transaction will be inverted to keep the amount positive and from and to consistent.
     */
    fun recordTransaction(
        from: Ledger,
        to: Ledger,
        amount: Int,
        type: TransactionType,
        subType: TransactionSubType? = null,
        detail: String? = null
    ) {
        if (amount < 0) return recordTransaction(to, from, amount, type, subType, detail)

        val transaction = MoneyTransaction(
            ledgerFrom = from.id,
            ledgerTo = to.id,
            type = type,
            subType = subType,
            detail = detail,
            date = Instant.now(),
            amount = amount
        )

        repository.recordNewTransaction(transaction)
    }
}