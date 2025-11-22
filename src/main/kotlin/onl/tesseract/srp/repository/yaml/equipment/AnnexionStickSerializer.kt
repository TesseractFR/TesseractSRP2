package onl.tesseract.srp.repository.yaml.equipment

import onl.tesseract.lib.persistence.yaml.equipment.InvocableGenericSerializer
import onl.tesseract.srp.util.AnnexionStickInvocable
import onl.tesseract.srp.service.territory.campement.CampementService
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration

class AnnexionStickSerializer(
    private val campementService: CampementService
) : InvocableGenericSerializer<AnnexionStickInvocable>() {

    override fun serialize(value: AnnexionStickInvocable): YamlConfiguration {
        val yaml = YamlConfiguration()
        writeGenericProps(yaml, value)
        return yaml
    }

    override fun deserialize(yaml: ConfigurationSection): AnnexionStickInvocable {
        val uuid = parsePlayerID(yaml)
        val invoked = parseInvoked(yaml)
        val handSlot = parseHandSlot(yaml)

        return AnnexionStickInvocable(
            uuid,
            isInvoked = invoked,
            handSlot = handSlot
        )
    }
}
