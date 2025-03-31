package onl.tesseract.srp.controller.command.job

import onl.tesseract.commandBuilder.CommandContext
import onl.tesseract.commandBuilder.CommandInstanceProvider
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.srp.controller.menu.job.mission.JobMissionMenu
import onl.tesseract.srp.service.item.CustomItemService
import onl.tesseract.srp.service.job.mission.JobMissionService
import onl.tesseract.srp.service.player.SrpPlayerService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component as SpringComponent

@Command(name = "missions", description = "Ouvrir le menu des missions", playerOnly = true)
@SpringComponent
class JobMissionCommand(
    provider: CommandInstanceProvider,
    private val missionService: JobMissionService,
    private val playerService: SrpPlayerService,
    private val customItemService: CustomItemService
) : CommandContext(provider){

    @CommandBody
    fun show(sender: Player) {
        JobMissionMenu(sender.uniqueId, missionService, playerService, customItemService).open(sender)
    }
}
