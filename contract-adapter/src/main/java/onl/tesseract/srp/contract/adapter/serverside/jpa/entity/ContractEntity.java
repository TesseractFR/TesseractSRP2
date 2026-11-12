package onl.tesseract.srp.contract.adapter.serverside.jpa.entity;

import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.contract.ContractType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_contracts", indexes = @Index(name = "idx_contracts_expires_at", columnList = "expires_at"))
public class ContractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractType type;

    @Column(nullable = false)
    private String job;

    @Column(nullable = false)
    private String material;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Quality minimalQuality;

    @Column(nullable = false)
    private int reward;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Version
    private long version;

    public static ContractEntity fromDomain(Contract contract) {
        return new ContractEntity(contract.id(), contract.type(), contract.job(), contract.material().value(), contract.quantity(),
                contract.minimalQuality(), contract.reward(), contract.expiresAt(), 0L);
    }

    public Contract toDomain() {
        return new Contract(id, type, job, new MaterialName(material), quantity, minimalQuality, reward,
                expiresAt);
    }
}