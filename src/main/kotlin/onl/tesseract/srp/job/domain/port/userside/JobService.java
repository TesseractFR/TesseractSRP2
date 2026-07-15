package onl.tesseract.srp.job.domain.port.userside;

import onl.tesseract.srp.job.domain.model.Job;
import onl.tesseract.srp.job.domain.model.JobName;
import onl.tesseract.srp.job.domain.port.serverside.JobRepository;

import java.util.List;

public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }


    public Job getJob(JobName jobName) {
        return jobRepository.findByName(jobName);
    }

    public List<Job> listJobs() {
        return jobRepository.findAll();
    }


}
