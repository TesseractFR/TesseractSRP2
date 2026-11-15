package onl.tesseract.srp.contract.domain.model.contract;

import lombok.NonNull;
import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;

import java.time.Instant;

/** Contrat enregistré, décrivant les matériaux à livrer, leur destinataire et leur récompense. */
public record Contract(@NonNull ContractId id, @NonNull ContractType type, @NonNull ContractClient client,
                       @NonNull MaterialName material, int quantity, @NonNull Quality minimalQuality,
                       @NonNull MoneyValue reward, @NonNull Instant expiresAt) {

    /** Vérifie que la quantité demandée est au moins égale à un. */
    public Contract {
        if (quantity < 1) {
            throw new IllegalArgumentException("La quantité d'un contrat doit être au moins 1 : " + quantity);
        }
    }

    /** @return {@code true} si la date d'expiration est atteinte ou dépassée à l'instant fourni */
    public boolean isExpiredAt(Instant now) {
        return !expiresAt.isAfter(now);
    }

    /** @return {@code true} si le contrat doit être livré au destinataire fourni */
    public boolean isFor(ContractClient candidate) {
        return client.equals(candidate);
    }
}
