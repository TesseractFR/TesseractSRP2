package onl.tesseract.srp.repository.yaml.equipment.annexionStick

import onl.tesseract.lib.equipment.Invocable
import onl.tesseract.lib.persistence.yaml.equipment.InvocableGenericSerializer
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.util.UUID

open class AnnexionStickSerializer<T : Invocable>(
    private val factory: (playerId: UUID, isInvoked: Boolean, handSlot: Int) -> T
) : InvocableGenericSerializer<T>() {

    override fun serialize(value: T): YamlConfiguration {
        val yaml = YamlConfiguration()
        writeGenericProps(yaml, value)
        return yaml
    }

    override fun deserialize(yaml: ConfigurationSection): T {
        val uuid = parsePlayerID(yaml)
        val invoked = parseInvoked(yaml)
        val handSlot = parseHandSlot(yaml)

        return factory(uuid, invoked, handSlot)
    }
}
