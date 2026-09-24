package onl.tesseract.srp.repository.hibernate;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import onl.tesseract.srp.domain.money.ledger.MoneyTransaction;
import onl.tesseract.srp.domain.money.ledger.TransactionType;
import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "t_money_transaction", indexes = {
    @Index(name = "ledger_from_idx", columnList = "ledger_from", unique = false),
    @Index(name = "ledger_to_idx", columnList = "ledger_to", unique = false),
    @Index(name = "date_idx", columnList = "date", unique = false)
})
public class MoneyTransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ledger_from")
    private UUID ledgerFrom;

    @Column(name = "ledger_to")
    private UUID ledgerTo;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private String subType;
    private String detail;
    private Instant date;
    private int amount;

    public MoneyTransactionEntity() {}

    public MoneyTransactionEntity(Long id, UUID ledgerFrom, UUID ledgerTo, TransactionType type, String subType, String detail, Instant date, int amount) {
        this.id = id;
        this.ledgerFrom = ledgerFrom;
        this.ledgerTo = ledgerTo;
        this.type = type;
        this.subType = subType;
        this.detail = detail;
        this.date = date;
        this.amount = amount;
    }

    public onl.tesseract.srp.domain.money.ledger.MoneyTransaction toDomain() {
        return new onl.tesseract.srp.domain.money.ledger.MoneyTransaction(
            ledgerFrom, ledgerTo, type, null, detail, date, amount
        );
    }

    public static MoneyTransactionEntity fromDomain(MoneyTransaction transaction) {
        return new MoneyTransactionEntity(
                null,
                transaction.getLedgerFrom(),
                transaction.getLedgerTo(),
                transaction.getType(),
                transaction.getSubType() != null ? transaction.getSubType().toString() : null,
                transaction.getDetail(),
                transaction.getDate(),
                transaction.getAmount()
        );
    }
}

