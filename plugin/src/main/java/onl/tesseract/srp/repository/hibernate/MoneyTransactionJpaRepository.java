package onl.tesseract.srp.repository.hibernate;

import onl.tesseract.srp.domain.money.ledger.MoneyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface MoneyTransactionJpaRepository extends JpaRepository<MoneyTransactionEntity, UUID> {
}

