package onl.tesseract.srp.contract.domain.model.profile;

import java.util.List;

public record PlayerContractProfile(List<JobProfile> jobs) {
    public PlayerContractProfile {
        jobs = List.copyOf(jobs);
    }
}
