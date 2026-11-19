package onl.tesseract.srp.contract.adapter.serverside.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.NoArgsConstructor;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.contract.domain.model.contract.ContractType;

import java.time.Instant;

/** Entité JPA représentant un contrat persisté dans la table {@code t_contracts}. */
@Entity
@Data
@NoArgsConstructor
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
}
