package onl.tesseract.srp.controller.command.campement

import jakarta.annotation.PostConstruct
import net.kyori.adventure.text.Component
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.lib.equipment.EquipmentMenu
import onl.tesseract.lib.equipment.EquipmentService
import onl.tesseract.lib.persistence.yaml.equipment.EquipmentYamlRepository
import onl.tesseract.lib.util.ChatFormats
import onl.tesseract.srp.domain.campement.AnnexionStickInvocable
import onl.tesseract.srp.repository.yaml.equipment.AnnexionStickSerializer
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
@Command(name = "stick", playerOnly = true)
class AnnexionStickCommand(
    private val campementService: CampementService,
    private val equipmentService: EquipmentService
) {
    @PostConstruct
    fun registerSerializer() {
        EquipmentYamlRepository.registerTypeSerializer(
            "annexion_stick",
            AnnexionStickSerializer(campementService, equipmentService)
        )
    }

    @CommandBody
    fun onCommand(sender: Player) {
        val equipment = equipmentService.getEquipment(sender.uniqueId)
        val existing = equipment.get(AnnexionStickInvocable::class.java)

        val invocable = existing ?: AnnexionStickInvocable(sender.uniqueId, campementService, equipmentService).also {
            equipmentService.add(sender.uniqueId, it)
        }
        val inv = sender.inventory
        val hasFreeSlotInHotbar = (0..8).any { inv.getItem(it) == null }

        if (hasFreeSlotInHotbar) {
            equipmentService.invoke(sender, AnnexionStickInvocable::class.java)
            sender.sendMessage(ChatFormats.CHAT.append(Component.text("Tu as reçu un Bâton d'Annexion.")))
        } else {
            val menu = EquipmentMenu(sender, equipmentService)
            menu.mainHandInvocationMenu(invocable, sender)
        }
    }
}
