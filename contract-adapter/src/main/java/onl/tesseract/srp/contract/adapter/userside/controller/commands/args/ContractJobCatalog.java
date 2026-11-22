package onl.tesseract.srp.contract.adapter.userside.controller.commands.args;

import onl.tesseract.srp.contract.adapter.serverside.skill.SkillCatalogPort;
import onl.tesseract.srp.contract.domain.model.contract.ContractClient;
import onl.tesseract.srp.job.domain.port.userside.JobCatalogService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Fournit les noms canoniques des métiers et compétences qui peuvent recevoir des contrats. */
@Component
public class ContractJobCatalog {

    private final JobCatalogService jobCatalogService;
    private final SkillCatalogPort skillCatalogPort;

    public ContractJobCatalog(JobCatalogService jobCatalogService, SkillCatalogPort skillCatalogPort) {
        this.jobCatalogService = jobCatalogService;
        this.skillCatalogPort = skillCatalogPort;
    }

    public List<String> findAllNames() {
        List<String> names = new ArrayList<>();
        jobCatalogService.findAllJobs().forEach(job -> names.add(job.jobName().value()));
        skillCatalogPort.findAllSkills().forEach(skill -> names.add(skill.name().value()));
        return names;
    }

    /** @return le destinataire dont le nom correspond, sans tenir compte de la casse */
    public Optional<ContractClient> findByName(String input) {
        return findAllNames().stream()
                .filter(name -> name.equalsIgnoreCase(input))
                .findFirst()
                .map(ContractClient::new);
    }
}
