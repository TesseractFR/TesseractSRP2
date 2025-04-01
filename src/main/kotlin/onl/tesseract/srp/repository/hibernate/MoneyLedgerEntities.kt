package onl.tesseract.srp.repository.hibernate

import jakarta.persistence.*
import onl.tesseract.srp.domain.money.ledger.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "t_money_ledger")
class MoneyLedgerEntity(
    @Id
    val id: UUID,
    @Enumerated(EnumType.STRING)
    val ledgerType: LedgerType,
) {

    fun toDomain(): Ledger = Ledger(id, ledgerType)
}

fun Ledger.toEntity(): MoneyLedgerEntity = MoneyLedgerEntity(id, type)

@Entity
@Table(name = "t_money_transaction", indexes = [
    Index(name = "ledger_from_idx", columnList = "ledger_from", unique = false),
    Index(name = "ledger_to_idx", columnList = "ledger_to", unique = false),
    Index(name = "date_idx", columnList = "date", unique = false),
])
class MoneyTransactionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "ledger_from")
    val ledgerFrom: UUID,
    @Column(name = "ledger_to")
    val ledgerTo: UUID,
    @Enumerated(EnumType.STRING)
    val type: TransactionType,
    val subType: String?,
    val detail: String?,
    val date: Instant,
    val amount: Int,
)

fun MoneyTransaction.toEntity(): MoneyTransactionEntity = MoneyTransactionEntity(
    null,
    ledgerFrom,
    ledgerTo,
    type,
    subType?.toString(),
    detail,
    date,
    amount
)