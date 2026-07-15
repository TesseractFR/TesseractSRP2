package onl.tesseract.srp.job.domain.model;

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

    public record TalentProgressions(
            Map<TalentName, Integer> talentLevels) {}
}
