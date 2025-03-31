package onl.tesseract.srp.repository.hibernate

import onl.tesseract.lib.repository.Repository
import onl.tesseract.srp.domain.money.ledger.Ledger
import onl.tesseract.srp.domain.money.ledger.MoneyTransaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.*

interface MoneyLedgerRepository : Repository<Ledger, UUID> {

    fun recordNewTransaction(transaction: MoneyTransaction)
}

@Component
class MoneyLedgerJpaAdapter(
    private val ledgerRepository: MoneyLedgerJpaRepository,
    private val moneyRepository: MoneyTransactionJpaRepository
) : MoneyLedgerRepository {

    override fun getById(id: UUID): Ledger? {
        return ledgerRepository.findByIdOrNull(id)?.toDomain()
    }

    override fun save(entity: Ledger): Ledger {
        return ledgerRepository.save(entity.toEntity()).toDomain()
    }

    override fun idOf(entity: Ledger): UUID = entity.id

    override fun recordNewTransaction(transaction: MoneyTransaction) {
        moneyRepository.save(transaction.toEntity())
    }
}

@org.springframework.stereotype.Repository
interface MoneyLedgerJpaRepository : JpaRepository<MoneyLedgerEntity, UUID>

@org.springframework.stereotype.Repository
interface MoneyTransactionJpaRepository : JpaRepository<MoneyTransactionEntity, UUID>