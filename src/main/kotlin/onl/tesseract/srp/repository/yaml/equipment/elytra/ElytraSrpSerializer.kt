package onl.tesseract.srp.repository.yaml.equipment.elytra

import onl.tesseract.lib.event.equipment.invocable.Elytra
import onl.tesseract.lib.persistence.yaml.equipment.ElytraSerializer
import onl.tesseract.srp.repository.yaml.equipment.SrpInvocableSerializer
import org.springframework.stereotype.Component

@Component
class ElytraSrpSerializer : ElytraSerializer(), SrpInvocableSerializer {

    override val typeKey: String = Elytra::class.java.simpleName

}
