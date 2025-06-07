package onl.tesseract.srp.controller.command.staff

import onl.tesseract.commandBuilder.annotation.Argument
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.lib.command.argument.IntegerCommandArgument
import onl.tesseract.lib.command.argument.PlayerArg
import onl.tesseract.lib.equipment.EquipmentService
import onl.tesseract.lib.event.equipment.invocable.Elytra
import onl.tesseract.srp.controller.command.argument.ElytraUpgradeArg
import org.bukkit.command.CommandSender
import org.springframework.stereotype.Component

@Component
@Command(name = "elytraUpgrade")
class ElytraStaffCommand(
    private val equipmentService: EquipmentService
) {
    @Command(name = "set", description = "Définir le niveau des améliorations élytras pour les joueurs")
    fun set(
        sender: CommandSender,
        @Argument("player") playerArg: PlayerArg,
        @Argument("upgrade") upgradeArg: ElytraUpgradeArg,
        @Argument("level") level: IntegerCommandArgument
    ) {
        require(level.get() in 0..9) {
            sender.sendMessage("Le niveau doit être compris entre 0 et 9.")
            "Le niveau doit être compris entre 0 et 9."
        }

        val equipment = equipmentService.getEquipment(playerArg.get().uniqueId)
        val elytra = equipment.get(Elytra::class.java)
        if (elytra == null) {
            sender.sendMessage("${playerArg.get().name} ne possède pas d'élytra personnalisée.")
            return
        }

        elytra.setLevel(upgradeArg.get(), level.get())
        equipmentService.saveEquipment(equipment)
        sender.sendMessage("Amélioration ${upgradeArg.get().displayName} de ${playerArg.get().name} " +
                "définie au niveau ${level.get()}.")
    }
}