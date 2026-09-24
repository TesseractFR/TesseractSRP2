package onl.tesseract.srp.repository.hibernate;

import onl.tesseract.srp.domain.money.ledger.Ledger;
import onl.tesseract.srp.domain.money.ledger.MoneyTransaction;
import onl.tesseract.srp.repository.generic.MoneyLedgerRepository;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class MoneyLedgerJpaAdapter implements MoneyLedgerRepository {

    private final MoneyLedgerJpaRepository ledgerRepository;
    private final MoneyTransactionJpaRepository moneyRepository;

    public MoneyLedgerJpaAdapter(MoneyLedgerJpaRepository ledgerRepository, MoneyTransactionJpaRepository moneyRepository) {
        this.ledgerRepository = ledgerRepository;
        this.moneyRepository = moneyRepository;
    }

    @Override
    public Ledger getById(UUID id) {
        return ledgerRepository.findById(id).map(MoneyLedgerEntity::toDomain).orElse(null);
    }

    @Override
    public Ledger save(Ledger entity) {
        MoneyLedgerEntity saved = ledgerRepository.save(MoneyLedgerEntity.fromDomain(entity));
        return saved.toDomain();
    }

    @Override
    public UUID idOf(Ledger entity) {
        return entity.getId();
    }

    @Override
    public void recordNewTransaction(MoneyTransaction transaction) {
        moneyRepository.save(MoneyTransactionEntity.fromDomain(transaction));
    }
}

