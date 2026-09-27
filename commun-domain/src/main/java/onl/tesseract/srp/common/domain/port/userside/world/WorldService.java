package onl.tesseract.srp.common.domain.port.userside.world;

import onl.tesseract.srp.common.domain.model.world.SrpWorld;
import onl.tesseract.srp.common.domain.model.world.WorldName;


public class WorldService {

    /**
     * Gets the SrpWorld for a Bukkit world, or null if not found.
     */
    public SrpWorld getSrpWorld(WorldName worldName) {
        for (SrpWorld srpWorld : SrpWorld.values()) {
            if (srpWorld.getBukkitName().value().equals(worldName.value())) {
                return srpWorld;
            }
        }
        return null;
    }
}

