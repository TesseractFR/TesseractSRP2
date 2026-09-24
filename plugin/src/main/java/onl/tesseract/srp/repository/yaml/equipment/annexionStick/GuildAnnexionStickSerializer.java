package onl.tesseract.srp.repository.yaml.equipment.annexionStick;

import onl.tesseract.srp.repository.yaml.equipment.SrpInvocableSerializer;
import onl.tesseract.srp.util.equipment.annexionStick.CampementAnnexionStickInvocable;
import onl.tesseract.srp.util.equipment.annexionStick.GuildAnnexionStickInvocable;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Serializer for Guild annexation sticks.
 */
@Component
public class GuildAnnexionStickSerializer extends AnnexionStickSerializer<GuildAnnexionStickInvocable>
        implements SrpInvocableSerializer {

    @Override
    public String getTypeKey() {
        return GuildAnnexionStickInvocable.class.getSimpleName();
    }

    @Override
    protected GuildAnnexionStickInvocable factory(UUID uuid, boolean invoked, int handSlot) {
        return new GuildAnnexionStickInvocable(uuid, invoked, handSlot);
    }}

