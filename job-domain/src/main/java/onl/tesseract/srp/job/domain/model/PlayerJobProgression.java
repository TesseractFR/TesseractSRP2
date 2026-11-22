package onl.tesseract.srp.job.domain.model;

import onl.tesseract.srp.job.domain.model.talent.Talent;
import onl.tesseract.srp.job.domain.model.talent.TalentName;

import java.util.Map;

public record PlayerJobProgression(
        JobName jobName,
        TalentProgressions talentProgressions,
        int availableTalentPoint,
        int totalTalentPoint
) {

    public PlayerJobProgression(JobName jobName) {
        this(jobName, new TalentProgressions(Map.of()), 0, 0);
    }

    public int getTalentLevel(TalentName talentName) {
        return talentProgressions.talentLevels().getOrDefault(talentName, 0);
    }

    /**
     * Indique si un talent peut être débloqué : il n'a aucun parent, ou chacun de ses parents
     * a au moins un niveau dans cette progression.
     *
     * @param talent le talent
     * @return {@code true} si le talent est accessible
     */
    public boolean isTalentAvailable(Talent talent) {
        return talent.parents().stream().allMatch(parent -> getTalentLevel(parent) > 0);
    }

    public record TalentProgressions(
            Map<TalentName, Integer> talentLevels) {}
}
