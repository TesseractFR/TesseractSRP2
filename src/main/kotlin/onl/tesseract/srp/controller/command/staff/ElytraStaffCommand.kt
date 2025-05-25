package onl.tesseract.srp.controller.command.staff

import onl.tesseract.commandBuilder.annotation.Argument
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.Perm
import onl.tesseract.lib.command.argument.IntegerCommandArgument
import onl.tesseract.lib.command.argument.PlayerArg
import onl.tesseract.lib.equipment.EquipmentService
import onl.tesseract.lib.event.equipment.invocable.Elytra
import onl.tesseract.srp.controller.command.argument.ElytraUpgradeArg
import onl.tesseract.srp.service.elytra.ElytraUpgradeService
import org.bukkit.command.CommandSender
import org.springframework.stereotype.Component

@Component
@Command(name = "elytraUpgrade", permission = Perm("staff"), playerOnly = true)
class ElytraStaffCommand(
    private val elytraUpgradeService: ElytraUpgradeService,
    private val equipmentService: EquipmentService
) {
    @Command(name = "set", description = "Définir le niveau des améliorations élytras pour les joueurs")
    fun set(
        sender: CommandSender,
        @Argument("player") playerArg: PlayerArg,
        @Argument("upgrade") upgradeArg: ElytraUpgradeArg,
        @Argument("level") level: IntegerCommandArgument
    ) {
        val player = playerArg.get()
        val targetUUID = player.uniqueId
        val upgrade = upgradeArg.get()
        val levelValue = level.get()
        require(levelValue in 0..9) {
            sender.sendMessage("Le niveau doit être compris entre 0 et 9.")
            "Le niveau doit être compris entre 0 et 9."
        }

        val equipment = equipmentService.getEquipment(targetUUID)
        val elytra = equipment.get(Elytra::class.java)
            ?: error("${player.name} ne possède pas d'élytra personnalisée.")

        elytraUpgradeService.setLevel(elytra, upgrade, levelValue)
        equipmentService.saveEquipment(equipment)
        sender.sendMessage("Amélioration ${upgrade.displayName} de ${player.name} définie au niveau $levelValue.")
    }
}