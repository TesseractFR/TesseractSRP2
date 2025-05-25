package onl.tesseract.srp.domain.money.ledger

import java.time.Instant
import java.util.*

/**
 * A transfer of money between two bank accounts. The server has its own account, to handle money creation (like through job rewards) and money deletion.
 */
data class MoneyTransaction(
    val ledgerFrom: UUID,
    val ledgerTo: UUID,
    val type: TransactionType,
    val subType: TransactionSubType?,
    val detail: String?,
    val date: Instant,
    /**
     * Positive
     */
    val amount: Int,
) {

    init {
        require(amount >= 0)
    }
}

enum class TransactionType {
    PlayerJob,
    /**
     * Staff commands
     */
    Staff,
    /**
     * Transactions related to a player's progression
     */
    Player,
    Guild,
}

interface TransactionSubType {

    enum class Job : TransactionSubType {
        Forgeron, Bucheron,
    }

    enum class Staff : TransactionSubType {
        Give,
    }

    enum class Player : TransactionSubType {
        Rank, Elytra
    }

    enum class Guild : TransactionSubType {
        Creation,
        BankTransfer,
    }
}

