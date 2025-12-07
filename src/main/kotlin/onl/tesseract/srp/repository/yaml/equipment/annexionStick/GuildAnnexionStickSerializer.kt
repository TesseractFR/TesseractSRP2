package onl.tesseract.srp.repository.yaml.equipment.annexionStick

import onl.tesseract.srp.repository.yaml.equipment.SrpInvocableSerializer
import onl.tesseract.srp.util.equipment.annexionStick.GuildAnnexionStickInvocable
import org.springframework.stereotype.Component

@Component
class GuildAnnexionStickSerializer :
    AnnexionStickSerializer<GuildAnnexionStickInvocable>(
        { uuid, invoked, handSlot -> GuildAnnexionStickInvocable(uuid, invoked, handSlot) }
    ),
    SrpInvocableSerializer {
    override val typeKey: String = GuildAnnexionStickInvocable::class.java.simpleName
}
