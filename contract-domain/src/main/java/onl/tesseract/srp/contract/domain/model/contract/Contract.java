package onl.tesseract.srp.contract.domain.model.contract;

import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.model.Quality;

import java.time.Instant;
import java.util.Objects;

public record Contract(Long id, PlayerID issuedTo, ContractType type, String job, String material, int quantity,
                       Quality minimalQuality, int reward, Instant issuedAt, Instant expiresAt) {

    public Contract {
        Objects.requireNonNull(issuedTo, "issuedTo cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(job, "job cannot be null");
        Objects.requireNonNull(material, "material cannot be null");
        Objects.requireNonNull(minimalQuality, "minimalQuality cannot be null");
        Objects.requireNonNull(issuedAt, "issuedAt cannot be null");
        Objects.requireNonNull(expiresAt, "expiresAt cannot be null");
        if (quantity < 1) throw new IllegalArgumentException("quantity must be >= 1");
        if (reward < 0) throw new IllegalArgumentException("reward must be >= 0");
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public boolean isForJob(String npcJob) {
        return job.equals(npcJob);
    }
}