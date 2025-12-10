package onl.tesseract.srp.controller.command.staff

import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.commandBuilder.annotation.Argument
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.lib.command.argument.IntegerCommandArgument
import onl.tesseract.lib.command.argument.OfflineUUIDPlayerArg
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.service.player.SrpPlayerService
import org.bukkit.command.CommandSender
import org.springframework.stereotype.Component

@Command(name = "illuminationPoint")
@Component
class IlluminationPointStaffCommand(private val playerService: SrpPlayerService) {

    @Command(name = "get")
    fun get(
        sender: CommandSender,
        @Argument("player") playerArg: OfflineUUIDPlayerArg
    ) {
        val player = playerService.getPlayer(playerArg.get().uniqueId)
        sender.sendMessage("${player.illuminationPoints} points d'illumination")
    }

    @Command(name = "give", description = "Donner des points d'illumination à un joueur")
    fun give(
        @Argument("player") playerArg: OfflineUUIDPlayerArg,
        @Argument("amount") amountArg: IntegerCommandArgument,
        sender: CommandSender
    ) {
        val result = playerService.giveIlluminationPoints(playerArg.get().uniqueId, amountArg.get())
        if (result) {
            sender.sendMessage(NamedTextColor.GREEN + "Points d'illumination ajoutés")
        } else {
            sender.sendMessage(NamedTextColor.RED + "Erreur lors de l'ajout")
        }
    }

    @Command(name = "take", description = "Retirer des points d'illumination à un joueur")
    fun take(
        @Argument("player") playerArg: OfflineUUIDPlayerArg,
        @Argument("amount") amountArg: IntegerCommandArgument,
        sender: CommandSender
    ) {
        val result = playerService.giveIlluminationPoints(playerArg.get().uniqueId, -amountArg.get())
        if (result) {
            sender.sendMessage(NamedTextColor.GREEN + "Points d'illumination retirés")
        } else {
            sender.sendMessage(NamedTextColor.RED + "Le joueur n'a pas assez de points")
        }
    }
}
