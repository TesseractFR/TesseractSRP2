package onl.tesseract.srp.controller.command.job;

import onl.tesseract.commandBuilder.CommandContext;
import onl.tesseract.commandBuilder.annotation.Command;
import onl.tesseract.srp.SrpCommandInstanceProvider;
import onl.tesseract.srp.controller.menu.job.JobSelectionMenu;
import onl.tesseract.srp.controller.menu.job.JobSkillMenu;
import onl.tesseract.srp.job.domain.model.PlayerID;
import onl.tesseract.srp.job.domain.port.userside.JobPlayerProgressionService;
import onl.tesseract.srp.job.domain.port.userside.JobService;
import onl.tesseract.srp.job.domain.port.userside.JobTalentTreeService;
import org.bukkit.entity.Player;
import org.springframework.stereotype.Component;

@Command(name = "jobskill", playerOnly = true)
public class JobSkillCommand extends CommandContext {
    private final JobService jobService;
    private final JobPlayerProgressionService jobPlayerProgressionService;
    private final JobTalentTreeService jobTalentTreeService;


    public JobSkillCommand(SrpCommandInstanceProvider provider, JobService jobService, JobPlayerProgressionService jobPlayerProgressionService, JobTalentTreeService jobTalentTreeService) {
        super(provider);
        this.jobService = jobService;
        this.jobPlayerProgressionService = jobPlayerProgressionService;
        this.jobTalentTreeService = jobTalentTreeService;
    }

    @Command
    public void openMenu(Player sender) {
        new JobSelectionMenu(
                "Compétences de métier",
                jobService,
                (viewer, job) -> new JobSkillMenu(
                        new PlayerID(viewer.getUniqueId()),
                        job,
                        jobTalentTreeService,
                        jobPlayerProgressionService
                ).open(viewer)
        ).open(sender);
    }
}
