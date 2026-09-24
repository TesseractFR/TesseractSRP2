package onl.tesseract.srp.service.money;

import onl.tesseract.srp.domain.money.ledger.Ledger;
import onl.tesseract.srp.domain.money.ledger.TransactionSubType;
import onl.tesseract.srp.domain.money.ledger.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Used to transfer money between two accounts atomically, while recording only one transaction.
 */
@Service
public class TransferService {
    private final MoneyLedgerService moneyLedgerService;

    public TransferService(MoneyLedgerService moneyLedgerService) {
        this.moneyLedgerService = moneyLedgerService;
    }

    /**
     * Transfer money between two accounts atomically in one transaction.
     */
    @Transactional
    public void transferMoney(int amount, TransactionType type, TransactionSubType subType, String details, TransferTransactionBuilder builder) {
        builder.build();

        Ledger from = builder.getFrom();
        Ledger to = builder.getTo();
        if (from == null) throw new IllegalStateException("Missing from side of transaction");
        if (to == null) throw new IllegalStateException("Missing to side of transaction");

        moneyLedgerService.recordTransaction(from, to, builder.getAmount(), builder.getType(), builder.getSubType(), builder.getDetails());
    }

    public static class TransferTransactionBuilder {
        private final int amount;
        private final TransactionType type;
        private final TransactionSubType subType;
        private final String details;
        private Ledger from;
        private Ledger to;

        public TransferTransactionBuilder(int amount, TransactionType type, TransactionSubType subType, String details) {
            if (amount <= 0) throw new IllegalArgumentException("Amount must be > 0");
            this.amount = amount;
            this.type = type;
            this.subType = subType;
            this.details = details;
        }

        public int getAmount() { return amount; }
        public TransactionType getType() { return type; }
        public TransactionSubType getSubType() { return subType; }
        public String getDetails() { return details; }

        public Ledger getFrom() { return from; }
        public void setFrom(Ledger from) {
            if (this.from != null) throw new IllegalStateException("from side of transaction already set");
            this.from = from;
        }

        public Ledger getTo() { return to; }
        public void setTo(Ledger to) {
            if (this.to != null) throw new IllegalStateException("to side of transaction already set");
            this.to = to;
        }

        public void build() {
            // No-op, kept for builder pattern compatibility
        }
    }
}

