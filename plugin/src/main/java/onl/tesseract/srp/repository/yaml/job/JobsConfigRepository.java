package onl.tesseract.srp.repository.yaml.job;

import onl.tesseract.lib.exception.ConfigurationException;
import onl.tesseract.srp.domain.job.EnumJob;
import onl.tesseract.srp.domain.job.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

/**
 * Repository for loading job configurations from YAML files.
 */
@Component
public class JobsConfigRepository {
    private static final Logger logger = LoggerFactory.getLogger(JobsConfigRepository.class);
    private static final Yaml yaml = new Yaml();

    private Map<EnumJob, Job> jobs;

    private Map<EnumJob, Job> loadJobs() {
        try(InputStream input = Files.newInputStream(Path.of("plugins/Tesseract/jobs.yml"))) {
            JobsConfig jobsConfig = yaml.loadAs(input, JobsConfig.class);
            return jobsConfig.toDomain();
        } catch (ConfigurationException e) {
            logger.error("Error while loading jobs", e);
            throw e;
        } catch (IOException e) {
            throw new ConfigurationException("The file jobs.yml doesn't exist!");
        }
    }

    public Map<EnumJob, Job> getJobs() {
        if (jobs == null) {
            try {
                jobs = loadJobs();
            } catch (ConfigurationException e) {
                logger.error("Error while loading jobs", e);
                jobs = Collections.emptyMap();
            }
        }
        return jobs;
    }
}

