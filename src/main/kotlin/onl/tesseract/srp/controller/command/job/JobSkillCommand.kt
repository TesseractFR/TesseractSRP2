package onl.tesseract.srp.controller.command.job

import onl.tesseract.commandBuilder.CommandContext
import onl.tesseract.commandBuilder.CommandInstanceProvider
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.srp.controller.menu.job.JobSelectionMenu
import onl.tesseract.srp.controller.menu.job.JobSkillMenu
import onl.tesseract.srp.service.job.PlayerJobService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component

@Component
@Command(name = "jobskill", description = "Ouvrir le menu des compétences de métier", playerOnly = true)
class JobSkillCommand(
    provider: CommandInstanceProvider,
    private val playerJobService: PlayerJobService
) : CommandContext(provider) {

    @CommandBody
    fun execute(player: Player) {
        JobSelectionMenu("Compétences de métier") { viewer, job ->
            JobSkillMenu(player.uniqueId, job, playerJobService).open(viewer)
        }.open(player)
    }
}
