package onl.tesseract.srp.job.adapter.serverside.jpa;

import onl.tesseract.srp.job.adapter.serverside.jpa.entity.JobPlayerProgressionEntity;
import onl.tesseract.srp.job.adapter.serverside.jpa.entity.JobPlayerTalentProgressionEntity;
import onl.tesseract.srp.job.domain.model.JobName;
import onl.tesseract.srp.job.domain.model.PlayerJobProgression;
import onl.tesseract.srp.job.domain.model.talent.TalentName;

import java.util.stream.Collectors;

/** Convertit l'entité JPA de progression d'un joueur dans un métier en modèle du domaine. */
public final class JobPlayerProgressionMapper {

    private JobPlayerProgressionMapper() {
    }

    /**
     * Construit la progression du domaine à partir de son entité.
     *
     * @param entity l'entité lue en base
     * @return la progression correspondante
     */
    public static PlayerJobProgression toDomain(JobPlayerProgressionEntity entity) {
        return new PlayerJobProgression(
                new JobName(entity.getId().getJobName()),
                new PlayerJobProgression.TalentProgressions(entity.getTalentProgressions().stream()
                        .collect(Collectors.toMap(it->
                                new TalentName(it.getTalentName()),
                                JobPlayerTalentProgressionEntity::getLevel
                        ))),
                entity.getAvailableTalentPoint(),
                entity.getTotalTalentPoint()
        );
    }
}
