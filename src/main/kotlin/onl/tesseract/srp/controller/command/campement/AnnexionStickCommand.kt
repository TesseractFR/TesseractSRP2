package onl.tesseract.srp.controller.command.campement

import jakarta.annotation.PostConstruct
import onl.tesseract.commandBuilder.annotation.Command
import onl.tesseract.commandBuilder.annotation.CommandBody
import onl.tesseract.lib.equipment.EquipmentService
import onl.tesseract.lib.persistence.yaml.equipment.EquipmentYamlRepository
import onl.tesseract.srp.domain.campement.AnnexationStickInvocable
import onl.tesseract.srp.repository.yaml.equipment.AnnexionStickSerializer
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.entity.Player
import org.springframework.stereotype.Component

@Component
@Command(name = "stick", playerOnly = true)
class AnnexionStickCommand(
    private val campementService: CampementService,
    private val equipmentService: EquipmentService
) {
    @PostConstruct
    fun registerSerializer() {
        EquipmentYamlRepository.registerTypeSerializer(
            "annexation_stick",
            AnnexionStickSerializer(campementService, equipmentService)
        )
    }

    @CommandBody
    fun onCommand(sender: Player) {
        val invocable = AnnexationStickInvocable(sender.uniqueId, campementService, equipmentService)
        equipmentService.add(sender.uniqueId, invocable)
        equipmentService.invoke(sender, AnnexationStickInvocable::class.java)

        sender.sendMessage("§6Tu as reçu un §eBâton d'Annexion§6 !")
    }
}
