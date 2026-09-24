package onl.tesseract.srp.util.equipment.annexionStick;

import org.bukkit.Material;

import java.util.UUID;

/**
 * Annexation stick for campement territory claims.
 */
public class CampementAnnexionStickInvocable extends AnnexionStickInvocable {

    public CampementAnnexionStickInvocable(UUID playerUUID) {
        super(playerUUID, false, 0);
    }

    public CampementAnnexionStickInvocable(UUID playerUUID, boolean isInvoked, int handSlot) {
        super(playerUUID, isInvoked, handSlot);
    }

    @Override
    public String getBaseCommand() {
        return "campement";
    }

    @Override
    public String getDisplayName() {
        return "Bâton d'Annexion (Campement)";
    }

    @Override
    public Material getMaterial() {
        return Material.STICK;
    }
}

