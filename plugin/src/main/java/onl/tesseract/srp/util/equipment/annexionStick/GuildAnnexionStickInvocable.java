package onl.tesseract.srp.util.equipment.annexionStick;

import org.bukkit.Material;

import java.util.UUID;

/**
 * Annexation stick for guild territory claims.
 */
public class GuildAnnexionStickInvocable extends AnnexionStickInvocable {

    public GuildAnnexionStickInvocable(UUID playerUUID) {
        this(playerUUID, false, 0);
    }

    public GuildAnnexionStickInvocable(UUID playerUUID, boolean isInvoked, int handSlot) {
        super(playerUUID, isInvoked, handSlot);
    }

    @Override
    public String getBaseCommand() {
        return "guild";
    }

    @Override
    public String getDisplayName() {
        return "Bâton d'Annexion (Guilde)";
    }

    @Override
    public Material getMaterial() {
        return Material.BREEZE_ROD;
    }
}

