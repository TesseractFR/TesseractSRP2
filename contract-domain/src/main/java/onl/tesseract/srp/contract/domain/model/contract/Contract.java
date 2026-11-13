package onl.tesseract.srp.contract.domain.model.contract;

import lombok.NonNull;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;

import java.time.Instant;

/** Contrat attribué à un joueur, décrivant les matériaux à livrer et leur récompense. */
public record Contract(Long id, @NonNull ContractType type, @NonNull String job,
                       @NonNull MaterialName material, int quantity, @NonNull Quality minimalQuality, int reward,
                       @NonNull Instant expiresAt) {

    /** Vérifie que la quantité est positive et que la récompense n'est pas négative. */
    public Contract {
        if (quantity < 1) throw new IllegalArgumentException("quantity must be >= 1");
        if (reward < 0) throw new IllegalArgumentException("reward must be >= 0");
    }

    /** @return {@code true} si l'instant fourni est postérieur à la date d'expiration */
    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    /** @return {@code true} si le contrat vise le métier fourni */
    public boolean isForJob(String npcJob) {
        return job.equals(npcJob);
    }
}
