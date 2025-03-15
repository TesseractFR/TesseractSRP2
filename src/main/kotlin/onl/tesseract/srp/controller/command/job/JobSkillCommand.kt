package onl.tesseract.srp.controller.command.job

import onl.tesseract.commandBuilder.CommandContext
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.srp.controller.menu.job.JobSkillJobSelectionMenu
import org.bukkit.entity.Player
import org.springframework.stereotype.Component

@Component
@Command(name = "jobskill", description = "Ouvrir le menu des compétences de métier", playerOnly = true)
class JobSkillCommand : CommandContext() {

    @CommandBody
    fun body(player: Player) {
        JobSkillJobSelectionMenu(player.uniqueId).open(player)
    }
}