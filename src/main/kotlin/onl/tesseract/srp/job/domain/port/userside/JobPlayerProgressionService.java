package onl.tesseract.srp.job.domain.port.userside;

import onl.tesseract.srp.job.domain.model.Job;
import onl.tesseract.srp.job.domain.model.JobName;
import onl.tesseract.srp.job.domain.model.PlayerID;
import onl.tesseract.srp.job.domain.model.PlayerJobProgression;
import onl.tesseract.srp.job.domain.model.talent.Talent;
import onl.tesseract.srp.job.domain.model.talent.TalentName;
import onl.tesseract.srp.job.domain.port.serverside.JobPlayerProgressionRepository;
import org.jetbrains.annotations.NotNull;

public class JobPlayerProgressionService {
    private final JobPlayerProgressionRepository jobPlayerProgressionRepository;

    public JobPlayerProgressionService(JobPlayerProgressionRepository jobPlayerProgressionRepository) {
        this.jobPlayerProgressionRepository = jobPlayerProgressionRepository;
    }


    public boolean isAvailable(PlayerID playerID,JobName jobName,Talent talent) {
        if(talent.parents().isEmpty())return true;
        for(TalentName parent : talent.parents()) {
            if(getTalentLevel(playerID, jobName, parent) <= 0) {
                return false;
            }
        }
        return true;
    }

    public int getTalentLevel(PlayerID playerID, JobName job, TalentName talentName) {
        return getJobProgression(playerID, job).getTalentLevel(talentName);
    }

    public int getTalentCost(PlayerID playerID, JobName job, Talent talent) {
        int talentLevel = getTalentLevel(playerID, job, talent.name());
        if(talentLevel >= talent.maxLevel()) {
            throw new IllegalStateException("Pas d'amélioration possible");
        }
        return talent.pricePerLevel().get(talentLevel+1);
    }

    public boolean canBuyUpgrade(PlayerID playerID, JobName jobName, Talent talent){
        return getTalentCost(playerID, jobName, talent) <= getJobProgression(playerID, jobName).availableTalentPoint();
    }


    private PlayerJobProgression getJobProgression(PlayerID playerID, JobName jobName) {
        return jobPlayerProgressionRepository.getPlayerJobProgression(playerID, jobName);
    }

    public boolean upgradeSkill(@NotNull PlayerID playerID, Talent skill) {
        return false;
    }
}
