package onl.tesseract.srp.job.adapter.userside.config;

import onl.tesseract.srp.job.domain.port.serverside.ItemRepository;
import onl.tesseract.srp.job.domain.port.serverside.JobPlayerProgressionRepository;
import onl.tesseract.srp.job.domain.port.serverside.JobRepository;
import onl.tesseract.srp.job.domain.port.serverside.JobSkillTreeRepository;
import onl.tesseract.srp.job.domain.port.userside.JobPlayerProgressionService;
import onl.tesseract.srp.job.domain.port.userside.JobCatalogService;
import onl.tesseract.srp.job.domain.port.userside.JobHarvestService;
import onl.tesseract.srp.job.domain.port.userside.JobTalentBonusService;
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
    public JobCatalogService jobCatalogService(JobRepository jobRepository) {
        return new JobCatalogService(jobRepository);
    }

    @Bean
    public JobTalentBonusService jobTalentBonusService(JobCatalogService jobCatalogService,
                                                       JobPlayerProgressionService jobPlayerProgressionService) {
        return new JobTalentBonusService(jobCatalogService, jobPlayerProgressionService);
    }

    @Bean
    public JobHarvestService jobHarvestService(JobCatalogService jobCatalogService,
                                               JobTalentBonusService jobTalentBonusService,
                                               ItemRepository itemRepository) {
        return new JobHarvestService(jobCatalogService, jobTalentBonusService, itemRepository);
    }

}
