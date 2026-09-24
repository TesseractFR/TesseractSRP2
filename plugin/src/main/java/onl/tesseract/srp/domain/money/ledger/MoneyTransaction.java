package onl.tesseract.srp.domain.money.ledger;

import java.time.Instant;
import java.util.UUID;

/**
 * A transfer of money between two bank accounts. The server has its own account, to handle money creation (like through job rewards) and money deletion.
 */
public class MoneyTransaction {
    private final UUID ledgerFrom;
    private final UUID ledgerTo;
    private final TransactionType type;
    private final TransactionSubType subType;
    private final String detail;
    private final Instant date;
    /**
     * Positive
     */
    private final int amount;

    public MoneyTransaction(UUID ledgerFrom, UUID ledgerTo, TransactionType type, TransactionSubType subType, String detail, Instant date, int amount) {
        if (amount < 0) throw new IllegalArgumentException("Amount must be >= 0");
        this.ledgerFrom = ledgerFrom;
        this.ledgerTo = ledgerTo;
        this.type = type;
        this.subType = subType;
        this.detail = detail;
        this.date = date;
        this.amount = amount;
    }

    public UUID getLedgerFrom() { return ledgerFrom; }
    public UUID getLedgerTo() { return ledgerTo; }
    public TransactionType getType() { return type; }
    public TransactionSubType getSubType() { return subType; }
    public String getDetail() { return detail; }
    public Instant getDate() { return date; }
    public int getAmount() { return amount; }
}

