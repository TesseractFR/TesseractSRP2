package onl.tesseract.srp.contract.domain.model.contract;

import lombok.NonNull;
import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.model.Quality;

import java.time.Instant;

public record Contract(Long id, @NonNull PlayerID issuedTo, @NonNull ContractType type, @NonNull String job,
                       @NonNull String material, int quantity, @NonNull Quality minimalQuality, int reward,
                       @NonNull Instant issuedAt, @NonNull Instant expiresAt) {

    public Contract {
        if (quantity < 1) throw new IllegalArgumentException("quantity must be >= 1");
        if (reward < 0) throw new IllegalArgumentException("reward must be >= 0");
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }

    public boolean isForJob(String npcJob) {
        return job.equals(npcJob);
    }
}