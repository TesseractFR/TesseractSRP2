package onl.tesseract.srp.job.domain.port.serverside;

import onl.tesseract.srp.job.domain.model.JobName;
import onl.tesseract.srp.job.domain.model.talenttree.TalentTree;

public interface JobSkillTreeRepository {

    TalentTree getTalentTree(JobName jobName);
}
