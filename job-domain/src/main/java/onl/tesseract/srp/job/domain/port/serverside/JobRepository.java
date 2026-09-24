package onl.tesseract.srp.job.domain.port.serverside;

import onl.tesseract.srp.job.domain.model.Job;
import onl.tesseract.srp.job.domain.model.JobName;

import java.util.List;

public interface JobRepository {
    List<Job> findAll();

    Job findByName(JobName jobName);
}
