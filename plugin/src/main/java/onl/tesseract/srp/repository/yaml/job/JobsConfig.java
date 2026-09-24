package onl.tesseract.srp.repository.yaml.job;

import onl.tesseract.srp.domain.item.CustomMaterial;
import onl.tesseract.srp.domain.job.BaseStat;
import onl.tesseract.srp.domain.job.EnumJob;
import onl.tesseract.srp.domain.job.Job;
import onl.tesseract.srp.domain.job.MissionTemplate;

import java.util.Collections;
import java.util.Map;

/**
 * Configuration for all jobs.
 */
public class JobsConfig {
    private Map<EnumJob, JobConfig> jobs;

    public JobsConfig() {
        this.jobs = Collections.emptyMap();
    }

    public JobsConfig(Map<EnumJob, JobConfig> jobs) {
        this.jobs = jobs;
    }

    public Map<EnumJob, JobConfig> getJobs() {
        return jobs;
    }

    public void setJobs(Map<EnumJob, JobConfig> jobs) {
        this.jobs = jobs;
    }

    public Map<EnumJob, Job> toDomain() {
        Map<EnumJob, Job> result = new java.util.HashMap<>();
        for (Map.Entry<EnumJob, JobConfig> entry : jobs.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toDomain(entry.getKey()));
        }
        return result;
    }

    /**
     * Configuration for a single job.
     */
    public static class JobConfig {
        private Map<CustomMaterial, BaseStat> baseStats;
        private java.util.List<MissionTemplate> missions;

        public JobConfig() {
            this.baseStats = Collections.emptyMap();
            this.missions = Collections.emptyList();
        }

        public JobConfig(Map<CustomMaterial, BaseStat> baseStats, java.util.List<MissionTemplate> missions) {
            this.baseStats = baseStats;
            this.missions = missions;
        }

        public Map<CustomMaterial, BaseStat> getBaseStats() {
            return baseStats;
        }

        public void setBaseStats(Map<CustomMaterial, BaseStat> baseStats) {
            this.baseStats = baseStats;
        }

        public java.util.List<MissionTemplate> getMissions() {
            return missions;
        }

        public void setMissions(java.util.List<MissionTemplate> missions) {
            this.missions = missions;
        }

        public Job toDomain(EnumJob enumJob) {
            return new Job(enumJob, baseStats, missions);
        }
    }
}

