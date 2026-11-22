package onl.tesseract.srp.job.adapter.userside.controller.command;

import onl.tesseract.commandBuilder.CommandContext;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.srp.common.adapter.SrpCommandInstanceProvider;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.job.domain.port.userside.JobPlayerProgressionService;
import onl.tesseract.srp.job.domain.port.userside.JobCatalogService;
import onl.tesseract.srp.job.domain.port.userside.JobTalentTreeService;
import org.bukkit.entity.Player;
import org.springframework.stereotype.Component;

@Component
@Command(name = "jobskill", playerOnly = true)
public class JobSkillCommand extends CommandContext {
    private final JobCatalogService jobCatalogService;
    private final JobPlayerProgressionService jobPlayerProgressionService;
    private final JobTalentTreeService jobTalentTreeService;


    public JobSkillCommand(SrpCommandInstanceProvider provider, JobCatalogService jobCatalogService, JobPlayerProgressionService jobPlayerProgressionService, JobTalentTreeService jobTalentTreeService) {
        super(provider);
        this.jobCatalogService = jobCatalogService;
        this.jobPlayerProgressionService = jobPlayerProgressionService;
        this.jobTalentTreeService = jobTalentTreeService;
    }

    @Command
    public void openMenu(Player sender) {
        new JobSelectionMenu(
                "Compétences de métier",
                jobCatalogService,
                (viewer, job) -> new JobSkillMenu(
                        new PlayerId(viewer.getUniqueId()),
                        job,
                        jobTalentTreeService,
                        jobPlayerProgressionService
                ).open(viewer)
        ).open(sender);
    }
}
