package onl.tesseract.srp.domain.job;

import onl.tesseract.srp.domain.item.CustomMaterial;
import java.util.UUID;

public class JobHarvestEvent {
    private final UUID playerID;
    private final EnumJob job;
    private final CustomMaterial material;

    public JobHarvestEvent(UUID playerID, EnumJob job, CustomMaterial material) {
        this.playerID = playerID;
        this.job = job;
        this.material = material;
    }

    public UUID getPlayerID() { return playerID; }
    public EnumJob getJob() { return job; }
    public CustomMaterial getMaterial() { return material; }
}

