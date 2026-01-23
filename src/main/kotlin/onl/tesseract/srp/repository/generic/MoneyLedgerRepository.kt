package onl.tesseract.srp.repository.generic

import onl.tesseract.lib.repository.Repository
import onl.tesseract.srp.domain.money.ledger.Ledger
import onl.tesseract.srp.domain.money.ledger.MoneyTransaction
import java.util.UUID

interface MoneyLedgerRepository : Repository<Ledger, UUID> {

    fun recordNewTransaction(transaction: MoneyTransaction)
}