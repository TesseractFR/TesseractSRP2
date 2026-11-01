package onl.tesseract.srp.job.domain.port.userside;

import lombok.NonNull;
import onl.tesseract.srp.job.domain.model.JobName;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.job.domain.model.PlayerJobProgression;
import onl.tesseract.srp.job.domain.model.talent.Talent;
import onl.tesseract.srp.job.domain.model.talent.TalentName;
import onl.tesseract.srp.job.domain.port.serverside.JobPlayerProgressionRepository;

public class JobPlayerProgressionService {
    private final JobPlayerProgressionRepository jobPlayerProgressionRepository;

    public JobPlayerProgressionService(JobPlayerProgressionRepository jobPlayerProgressionRepository) {
        this.jobPlayerProgressionRepository = jobPlayerProgressionRepository;
    }


    public boolean isAvailable(PlayerId playerID,JobName jobName,Talent talent) {
        if(talent.parents().isEmpty())return true;
        for(TalentName parent : talent.parents()) {
            if(getTalentLevel(playerID, jobName, parent) <= 0) {
                return false;
            }
        }
        return true;
    }

    public int getTalentLevel(PlayerId playerID, JobName job, TalentName talentName) {
        return getJobProgression(playerID, job).getTalentLevel(talentName);
    }

    public int getTalentCost(PlayerId playerID, JobName job, Talent talent) {
        int talentLevel = getTalentLevel(playerID, job, talent.name());
        if(talentLevel >= talent.maxLevel()) {
            throw new IllegalStateException("Pas d'amélioration possible");
        }
        return talent.pricePerLevel().get(talentLevel+1);
    }

    public boolean canBuyUpgrade(PlayerId playerID, JobName jobName, Talent talent){
        return getTalentCost(playerID, jobName, talent) <= getJobProgression(playerID, jobName).availableTalentPoint();
    }


    private PlayerJobProgression getJobProgression(PlayerId playerID, JobName jobName) {
        return jobPlayerProgressionRepository.getPlayerJobProgression(playerID, jobName);
    }

    public boolean upgradeSkill(@NonNull PlayerId playerID, Talent skill) {
        return false;
    }
}
