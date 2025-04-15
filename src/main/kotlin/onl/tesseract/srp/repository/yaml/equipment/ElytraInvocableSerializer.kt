package onl.tesseract.srp.repository.yaml.equipment

import onl.tesseract.lib.persistence.yaml.equipment.InvocableGenericSerializer
import onl.tesseract.srp.domain.elytra.ElytraInvocable
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration

class ElytraInvocableSerializer : InvocableGenericSerializer<ElytraInvocable>() {

    override fun serialize(value: ElytraInvocable): YamlConfiguration {
        val yaml = YamlConfiguration()
        writeGenericProps(yaml, value)
        return yaml
    }

    override fun deserialize(yaml: ConfigurationSection): ElytraInvocable {
        val uuid = parsePlayerID(yaml)
        val invoked = parseInvoked(yaml)
        val handSlot = parseHandSlot(yaml)

        return ElytraInvocable(
            playerUUID = uuid,
            isInvoked = invoked,
            handSlot = handSlot
        )
    }
}
