package onl.tesseract.srp.domain.money.ledger

import java.util.*

/**
 * All money transactions are made between 2 bank accounts, represented by a Ledger
 */
class Ledger(
    /**
     * Id of the bank account. For players, it will be the player's uuid.
     */
    val id: UUID,
    /**
     * Type of account behind the ledger.
     */
    val type: LedgerType,
)

enum class LedgerType {
    Player, Guild, Server
}