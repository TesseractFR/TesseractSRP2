package onl.tesseract.srp.contract.domain.model.contract;

import lombok.NonNull;
import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;

import java.time.Instant;

/**
 * Contrat généré mais pas encore enregistré. Il n'a pas d'identifiant : c'est le dépôt qui
 * l'attribue en le transformant en {@link Contract}.
 */
public record ContractDraft(@NonNull ContractType type, @NonNull ContractClient client, @NonNull MaterialName material,
                            int quantity, @NonNull Quality minimalQuality, @NonNull MoneyValue reward,
                            @NonNull Instant expiresAt) {
    /** Vérifie que la quantité demandée est au moins égale à un. */
    public ContractDraft {
        if (quantity < 1) {
            throw new IllegalArgumentException("La quantité d'un contrat doit être au moins 1 : " + quantity);
        }
    }
}
