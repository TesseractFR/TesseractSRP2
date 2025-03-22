package onl.tesseract.srp.repository.yaml.equipment

import onl.tesseract.lib.equipment.EquipmentService
import onl.tesseract.lib.persistence.yaml.equipment.InvocableGenericSerializer
import onl.tesseract.srp.domain.campement.AnnexionStickInvocable
import onl.tesseract.srp.service.campement.CampementService
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.util.*

class AnnexionStickSerializer(
    private val campementService: CampementService,
    private val equipmentService: EquipmentService
) : InvocableGenericSerializer<AnnexionStickInvocable>() {

    override fun serialize(value: AnnexionStickInvocable): YamlConfiguration {
        val yaml = YamlConfiguration()
        writeGenericProps(yaml, value)
        yaml["playerUUID"] = value.playerUUID.toString()
        yaml["invoked"] = value.isInvoked
        yaml["handSlot"] = value.handSlot
        return yaml
    }

    override fun deserialize(yaml: ConfigurationSection): AnnexionStickInvocable {
        val uuid = UUID.fromString(yaml.getString("playerUUID") ?: error("playerUUID missing"))
        val invoked = parseInvoked(yaml)
        val handSlot = parseHandSlot(yaml)

        return AnnexionStickInvocable(uuid, campementService, equipmentService).apply {
            isInvoked = invoked
            this.handSlot = handSlot
        }
    }
}
