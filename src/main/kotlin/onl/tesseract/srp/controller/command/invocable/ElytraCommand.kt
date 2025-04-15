package onl.tesseract.srp.controller.command.invocable

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.commandBuilder.CommandContext
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.lib.equipment.EquipmentService
import onl.tesseract.srp.SrpCommandInstanceProvider
import onl.tesseract.srp.domain.elytra.ElytraInvocable
import org.bukkit.entity.Player
import org.springframework.stereotype.Component as SpringComponent

@Command(name = "elytras", description = "Recevoir les Ailes Célestes.")
@SpringComponent
class ElytraCommand(
    private val equipmentService: EquipmentService,
    commandInstanceProvider: SrpCommandInstanceProvider
) : CommandContext(commandInstanceProvider) {

    @CommandBody
    fun onCommand(sender: Player) {
        val equipment = equipmentService.getEquipment(sender.uniqueId)
        val existing = equipment.get(ElytraInvocable::class.java)

        existing ?: ElytraInvocable(sender.uniqueId).also {
            equipmentService.add(sender.uniqueId, it)
        }

        equipmentService.invoke(sender, ElytraInvocable::class.java)
        sender.sendMessage(Component.text("Tu as reçu les Ailes Célestes !", NamedTextColor.GREEN))
    }
}
