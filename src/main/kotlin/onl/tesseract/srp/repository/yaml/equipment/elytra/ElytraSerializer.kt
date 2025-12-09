package onl.tesseract.srp.repository.yaml.equipment.elytra

import onl.tesseract.lib.event.equipment.invocable.Elytra
import onl.tesseract.lib.persistence.yaml.equipment.InvocableGenericSerializer
import onl.tesseract.srp.repository.yaml.equipment.SrpInvocableSerializer
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.springframework.stereotype.Component

@Component
class ElytraSerializer :
    InvocableGenericSerializer<Elytra>(),
    SrpInvocableSerializer {

    override val typeKey: String = Elytra::class.java.simpleName

    override fun serialize(value: Elytra): YamlConfiguration {
        val yaml = YamlConfiguration()
        writeGenericProps(yaml, value)
        yaml.set("autoGlide", value.autoGlide)
        yaml.set("speedLevel", value.speedLevel)
        yaml.set("protectionLevel", value.protectionLevel)
        yaml.set("boostChargeLevel", value.boostChargeLevel)
        yaml.set("recoveryLevel", value.recoveryLevel)
        yaml.set("currentCharges", value.currentCharges)
        yaml.set("rechargeProgress", value.rechargeProgress)
        return yaml
    }

    override fun deserialize(yaml: ConfigurationSection): Elytra {
        val uuid = parsePlayerID(yaml)
        val invoked = parseInvoked(yaml)
        val handSlot = parseHandSlot(yaml)
        val elytra = Elytra(uuid, invoked, handSlot)
        elytra.autoGlide = yaml.getBoolean("autoGlide", true)
        elytra.speedLevel = yaml.getInt("speedLevel", 0)
        elytra.protectionLevel = yaml.getInt("protectionLevel", 0)
        elytra.boostChargeLevel = yaml.getInt("boostChargeLevel", 0)
        elytra.recoveryLevel = yaml.getInt("recoveryLevel", 0)
        elytra.currentCharges = yaml.getInt("currentCharges", 0)
        elytra.rechargeProgress = yaml.getDouble("rechargeProgress", 0.0)
        return elytra
    }
}