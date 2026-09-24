package onl.tesseract.srp.repository.yaml.equipment.annexionStick;

import onl.tesseract.srp.repository.yaml.equipment.SrpInvocableSerializer;
import onl.tesseract.srp.util.equipment.annexionStick.CampementAnnexionStickInvocable;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Serializer for Campement annexation sticks.
 */
@Component
public class CampementAnnexionStickSerializer extends AnnexionStickSerializer<CampementAnnexionStickInvocable>
        implements SrpInvocableSerializer {

    @Override
    public String getTypeKey() {
        return CampementAnnexionStickInvocable.class.getSimpleName();
    }

    @Override
    protected CampementAnnexionStickInvocable factory(UUID uuid, boolean invoked, int handSlot) {
        return new CampementAnnexionStickInvocable(uuid, invoked, handSlot);
    }
}

