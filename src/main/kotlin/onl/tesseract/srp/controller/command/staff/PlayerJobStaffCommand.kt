package onl.tesseract.srp.controller.command.staff

import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.commandBuilder.annotation.Argument
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.Env
import onl.tesseract.lib.command.argument.IntegerCommandArgument
import onl.tesseract.lib.command.argument.PlayerArg
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.service.job.PlayerJobService
import onl.tesseract.srp.service.job.mission.JobMissionService
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.springframework.stereotype.Component

@Component
@Command(
    name = "job",
    args = [
        Argument("player", clazz = PlayerArg::class),
    ]
)
class PlayerJobStaffCommand(private val service: PlayerJobService) {

    @Command(name = "getLevel")
    fun getLevel(
        @Env(key = "player") player: Player,
        sender: CommandSender,
    ) {
        val progression = service.getPlayerJobProgression(player.uniqueId)
        sender.sendMessage(NamedTextColor.GREEN + "Niveau : ${progression.level}")
        sender.sendMessage(NamedTextColor.GREEN + "XP : ${progression.xp}")
        sender.sendMessage(NamedTextColor.GREEN + "Points de compétence : ${progression.skillPoints}")
    }

    @Command(name = "xp")
    @Component
    class XpCommand(private val service: PlayerJobService) {

        @Command
        fun give(
            @Env(key = "player") player: Player,
            @Argument("amount") amount: IntegerCommandArgument,
            sender: CommandSender,
        ) {
            service.addXp(player.uniqueId, amount.get())
            sender.sendMessage(NamedTextColor.GREEN + "XP ajouté !")
        }

        @Command
        fun remove(
            @Env(key = "player") player: Player,
            @Argument("amount") amount: IntegerCommandArgument,
            sender: CommandSender,
        ) {
            service.addXp(player.uniqueId, -amount.get())
            sender.sendMessage(NamedTextColor.GREEN + "XP retiré !")
        }

        @Command
        fun clear(
            @Env(key = "player") player: Player,
            sender: CommandSender,
        ) {
            service.clearXp(player.uniqueId)
            sender.sendMessage(NamedTextColor.GREEN + "XP remis à 0 !")
        }
    }

    @Command(name = "level")
    @Component
    class LevelCommand(private val service: PlayerJobService) {
        @Command
        fun give(
            @Env(key = "player") player: Player,
            @Argument("amount") amount: IntegerCommandArgument,
            sender: CommandSender,
        ) {
            service.addLevel(player.uniqueId, amount.get())
            sender.sendMessage(NamedTextColor.GREEN + "Niveau ajouté !")
        }

        @Command
        fun remove(
            @Env(key = "player") player: Player,
            @Argument("amount") amount: IntegerCommandArgument,
            sender: CommandSender,
        ) {
            service.addLevel(player.uniqueId, -amount.get())
            sender.sendMessage(NamedTextColor.GREEN + "Niveau retiré !")
        }
    }

    @Command(name = "skillpoints")
    @Component
    class SkillPointCommand(private val service: PlayerJobService) {
        @Command
        fun give(
            @Env(key = "player") player: Player,
            @Argument("amount") amount: IntegerCommandArgument,
            sender: CommandSender,
        ) {
            service.addSkillPoint(player.uniqueId, amount.get())
            sender.sendMessage(NamedTextColor.GREEN + "Points de compétence ajoutés !")
        }
    }

    @Command(name = "mission", description = "Gérer les missions du joueur")
    @Component
    class MissionCommand(private val missionService: JobMissionService) {

        @Command(description = "Voir les missions du joueur")
        fun list(
            @Env(key = "player") player: Player,
            sender: CommandSender,
        ) {
            val missions = missionService.getMissionsForPlayer(player.uniqueId)
            missions.forEach { sender.sendMessage(it.toString()) }
        }

        @Command(description = "Annuler une mission sans donner de récompense")
        fun cancel(
            @Argument("missionId") missionIdArg: IntegerCommandArgument,
            sender: CommandSender,
        ) {
            val canceled = missionService.cancelMission(missionIdArg.get().toLong())
            if (canceled)
                sender.sendMessage(NamedTextColor.GREEN + "Mission annulée.")
            else
                sender.sendMessage(NamedTextColor.RED + "Mission invalide.")
        }
    }
}