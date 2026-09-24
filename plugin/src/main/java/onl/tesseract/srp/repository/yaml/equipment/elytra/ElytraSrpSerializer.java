package onl.tesseract.srp.repository.yaml.equipment.elytra;

import onl.tesseract.lib.event.equipment.invocable.Elytra;
import onl.tesseract.lib.persistence.yaml.equipment.ElytraSerializer;
import onl.tesseract.srp.repository.yaml.equipment.SrpInvocableSerializer;
import org.springframework.stereotype.Component;

/**
 * Serializer for Elytra equipment.
 */
@Component
public class ElytraSrpSerializer extends ElytraSerializer implements SrpInvocableSerializer {

    @Override
    public String getTypeKey() {
        return Elytra.class.getSimpleName();
    }
}

