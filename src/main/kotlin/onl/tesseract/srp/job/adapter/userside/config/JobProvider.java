package onl.tesseract.srp.job.adapter.userside.config;

import onl.tesseract.srp.job.domain.port.serverside.JobPlayerProgressionRepository;
import onl.tesseract.srp.job.domain.port.serverside.JobRepository;
import onl.tesseract.srp.job.domain.port.serverside.JobSkillTreeRepository;
import onl.tesseract.srp.job.domain.port.userside.JobPlayerProgressionService;
import onl.tesseract.srp.job.domain.port.userside.JobService;
import onl.tesseract.srp.job.domain.port.userside.JobTalentTreeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobProvider {

    @Bean
    public JobTalentTreeService jobTalentTreeService(JobSkillTreeRepository jobSkillTreeRepository) {
        return new JobTalentTreeService(jobSkillTreeRepository);
    }

    @Bean
    public JobPlayerProgressionService jobPlayerProgressionService(JobPlayerProgressionRepository jobPlayerProgressionRepository){
        return new JobPlayerProgressionService(jobPlayerProgressionRepository);
    }

    @Bean
    public JobService jobService(JobRepository jobRepository){
        return new JobService(jobRepository);
    }

}
