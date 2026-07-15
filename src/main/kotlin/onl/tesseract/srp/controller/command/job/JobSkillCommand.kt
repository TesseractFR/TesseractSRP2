package onl.tesseract.srp.controller.command.job

import onl.tesseract.commandBuilder.CommandContext
import onl.tesseract.commandBuilder.CommandInstanceProvider
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.srp.controller.menu.job.JobSelectionMenu
import onl.tesseract.srp.controller.menu.job.JobSkillMenu
import onl.tesseract.srp.job.domain.model.PlayerID
import onl.tesseract.srp.job.domain.port.userside.JobPlayerProgressionService
import onl.tesseract.srp.job.domain.port.userside.JobService
import onl.tesseract.srp.job.domain.port.userside.JobTalentTreeService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component

@Component
@Command(name = "jobskill", description = "Ouvrir le menu des compétences de métier", playerOnly = true)
class JobSkillCommand(
    provider: CommandInstanceProvider,
    private val jobService: JobService,
    private val jobPlayerProgressionService: JobPlayerProgressionService,
    private val jobTalentTreeService: JobTalentTreeService
) : CommandContext(provider) {

    @CommandBody
    fun execute(player: Player) {
        JobSelectionMenu("Compétences de métier",jobService) { viewer, job ->
            JobSkillMenu(PlayerID(player.uniqueId), job, jobTalentTreeService, jobPlayerProgressionService).open(viewer)
        }.open(player)
    }
}
