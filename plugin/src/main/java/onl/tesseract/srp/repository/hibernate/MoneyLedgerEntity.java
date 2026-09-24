package onl.tesseract.srp.repository.hibernate;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import onl.tesseract.srp.domain.money.ledger.Ledger;
import onl.tesseract.srp.domain.money.ledger.LedgerType;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "t_money_ledger")
public class MoneyLedgerEntity {
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private LedgerType ledgerType;

    public MoneyLedgerEntity() {}

    public MoneyLedgerEntity(UUID id, LedgerType ledgerType) {
        this.id = id;
        this.ledgerType = ledgerType;
    }

    public Ledger toDomain() {
        return new Ledger(id, ledgerType);
    }
    public static MoneyLedgerEntity fromDomain(Ledger ledger) {
        return new MoneyLedgerEntity(ledger.getId(), ledger.getType());
    }

}

