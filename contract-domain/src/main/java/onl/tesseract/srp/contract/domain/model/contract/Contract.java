package onl.tesseract.srp.contract.domain.model.contract;

import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.model.Quality;

import java.time.Instant;

public record Contract(Long id, PlayerID issuedTo, ContractType type, String job, String material,
                       int quantity, Quality minimalQuality, int reward, int tier,
                       Instant issuedAt, Instant expiresAt, long version) {

    public Contract {
        if (issuedTo == null || type == null || job == null || material == null
                || minimalQuality == null || issuedAt == null || expiresAt == null) {
            throw new IllegalArgumentException("missing field");
        }
        if (quantity < 1) throw new IllegalArgumentException("quantity must be >= 1");
        if (reward < 0) throw new IllegalArgumentException("reward must be >= 0");
    }

    public Contract withId(Long newId) {
        return new Contract(newId, issuedTo, type, job, material, quantity, minimalQuality, reward, tier,
                issuedAt, expiresAt, version);
    }

    public Contract withReward(int newReward) {
        return new Contract(id, issuedTo, type, job, material, quantity, minimalQuality, newReward, tier,
                issuedAt, expiresAt, version);
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public boolean isForJob(String npcJob) {
        return job.equals(npcJob);
    }
}
