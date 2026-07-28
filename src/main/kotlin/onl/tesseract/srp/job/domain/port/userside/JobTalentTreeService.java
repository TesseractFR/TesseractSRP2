package onl.tesseract.srp.job.domain.port.userside;

import onl.tesseract.srp.job.domain.model.JobName;
import onl.tesseract.srp.job.domain.model.talenttree.TalentTree;
import onl.tesseract.srp.job.domain.port.serverside.JobSkillTreeRepository;

public class JobTalentTreeService {

    private final JobSkillTreeRepository jobSkillTreeRepository;

    public JobTalentTreeService(JobSkillTreeRepository jobSkillTreeRepository) {
        this.jobSkillTreeRepository = jobSkillTreeRepository;
    }

    public TalentTree getTalentTree(JobName jobName) {
        return jobSkillTreeRepository.getTalentTree(jobName);
    }
}
