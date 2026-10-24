package onl.tesseract.srp.contract.domain.model.profile;

import onl.tesseract.srp.contract.domain.model.Quality;

import java.util.List;

public record JobProfile(String job, int level, Quality maxQuality, List<UnlockedMaterial> materials) {
    public JobProfile {
        materials = List.copyOf(materials);
    }
}
