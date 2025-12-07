package onl.tesseract.srp.repository.yaml.equipment.annexionStick

import onl.tesseract.srp.repository.yaml.equipment.SrpInvocableSerializer
import onl.tesseract.srp.util.equipment.annexionStick.CampementAnnexionStickInvocable
import org.springframework.stereotype.Component

@Component
class CampementAnnexionStickSerializer :
    AnnexionStickSerializer<CampementAnnexionStickInvocable>(
        { uuid, invoked, handSlot -> CampementAnnexionStickInvocable(uuid, invoked, handSlot) }
    ),
    SrpInvocableSerializer {
    override val typeKey: String = CampementAnnexionStickInvocable::class.java.simpleName
}
