package onl.tesseract.srp.job.domain.model.mission;

import onl.tesseract.srp.common.domain.model.enums.Quality;
import java.util.UUID;

public record JobMission(long id, UUID playerId, String job, String material, int quantity, Quality minimalQuality,
                         int delivered, int reward) {
}

