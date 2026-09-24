package onl.tesseract.srp.repository.generic.job;

import onl.tesseract.srp.domain.job.PlayerJobProgression;
import java.util.UUID;

public interface PlayerJobProgressionRepository {
    PlayerJobProgression getById(UUID id);
    void save(PlayerJobProgression entity);
}

