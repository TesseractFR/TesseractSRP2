package onl.tesseract.srp.repository.generic;

import onl.tesseract.srp.domain.money.ledger.Ledger;
import onl.tesseract.srp.domain.money.ledger.MoneyTransaction;
import java.util.UUID;

public interface MoneyLedgerRepository {
    Ledger getById(UUID id);
    Ledger save(Ledger entity);
    UUID idOf(Ledger entity);
    void recordNewTransaction(MoneyTransaction transaction);
}

