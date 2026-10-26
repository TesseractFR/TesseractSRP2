package onl.tesseract.srp.contract.adapter.userside.controller.commands.args;

import onl.tesseract.srp.job.domain.port.userside.JobService;
import onl.tesseract.srp.skill.domain.port.serverside.SkillConfigRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ContractJobCatalog {

    private final JobService jobService;
    private final SkillConfigRepository skills;

    public ContractJobCatalog(JobService jobService, SkillConfigRepository skills) {
        this.jobService = jobService;
        this.skills = skills;
    }

    public List<String> names() {
        List<String> names = new ArrayList<>();
        jobService.listJobs().forEach(job -> names.add(job.jobName().value()));
        skills.getSkills().values().forEach(skill -> names.add(skill.name().value()));
        return names;
    }

    public Optional<String> find(String input) {
        return names().stream().filter(name -> name.equalsIgnoreCase(input)).findFirst();
    }
}