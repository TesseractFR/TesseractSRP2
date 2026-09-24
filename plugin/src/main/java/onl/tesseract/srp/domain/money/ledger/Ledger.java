package onl.tesseract.srp.domain.money.ledger;

import java.util.UUID;

/**
 * All money transactions are made between 2 bank accounts, represented by a Ledger
 */
public class Ledger {
    /**
     * Id of the bank account. For players, it will be the player's uuid.
     */
    private final UUID id;
    /**
     * Type of account behind the ledger.
     */
    private final LedgerType type;

    public Ledger(UUID id, LedgerType type) {
        this.id = id;
        this.type = type;
    }

    public UUID getId() { return id; }
    public LedgerType getType() { return type; }
}

