package onl.tesseract.srp.service.money;

import onl.tesseract.srp.domain.money.ledger.Ledger;
import onl.tesseract.srp.domain.money.ledger.LedgerType;
import onl.tesseract.srp.domain.money.ledger.MoneyTransaction;
import onl.tesseract.srp.domain.money.ledger.TransactionSubType;
import onl.tesseract.srp.domain.money.ledger.TransactionType;
import onl.tesseract.srp.repository.generic.MoneyLedgerRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Record money transactions. All money given to a player or a guild must be registered here.
 */
@Service
public class MoneyLedgerService {
    private final MoneyLedgerRepository repository;

    public MoneyLedgerService(MoneyLedgerRepository repository) {
        this.repository = repository;
    }

    /**
     * Get or create a player ledger
     */
    public Ledger getPlayerLedger(UUID playerID) {
        return new Ledger(playerID, LedgerType.Player);
    }

    public Ledger getServerLedger() {
        return new Ledger(UUID.fromString("00000000-0000-0000-0000-000000000000"), LedgerType.Server);
    }

    public Ledger createLedger(Ledger ledger) {
        if (repository.getById(ledger.getId()) == null) {
            repository.save(ledger);
        }
        return ledger;
    }

    public Ledger getGuildLedger(UUID guildID) {
        return createLedger(new Ledger(guildID, LedgerType.Guild));
    }

    /**
     * @param from The account giving the money. Use the server ledger with getServerLedger to create money (e.g for job rewards)
     * @param to The account receiving the money. Use the server ledger with getServerLedger to delete money (e.g to buy player ranks)
     * @param amount Amount of money to transfer. If negative, the transaction will be inverted to keep the amount positive and from and to consistent.
     */
    public void recordTransaction(Ledger from, Ledger to, int amount, TransactionType type, TransactionSubType subType, String detail) {
        if (amount < 0) {
            recordTransaction(to, from, -amount, type, subType, detail);
            return;
        }

        MoneyTransaction transaction = new MoneyTransaction(
            from.getId(),
            to.getId(),
            type,
            subType,
            detail,
            Instant.now(),
            amount
        );

        repository.recordNewTransaction(transaction);
    }

    public void recordTransaction(Ledger from, Ledger to, int amount, TransactionType type, TransactionSubType subType) {
        recordTransaction(from, to, amount, type, subType, null);
    }
}

