package onl.tesseract.srp.service.world;

import onl.tesseract.srp.common.domain.model.world.SrpWorld;
import onl.tesseract.srp.exception.WorldMissingException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.springframework.stereotype.Service;

/**
 * Provides access to SrpWorld enum values and Bukkit worlds.
 */
@Service
public class WorldService {

    /**
     * Gets the SrpWorld for a Bukkit world, or null if not found.
     */
    public SrpWorld getSrpWorld(World world) {
        for (SrpWorld srpWorld : SrpWorld.values()) {
            if (srpWorld.getBukkitName().equals(world.getName())) {
                return srpWorld;
            }
        }
        return null;
    }

    /**
     * Gets the SrpWorld for a location, or null if not found.
     */
    public SrpWorld getSrpWorld(Location location) {
        return getSrpWorld(location.getWorld());
    }

    /**
     * Gets the Bukkit world for an SrpWorld enum value.
     * @throws WorldMissingException If the world does not exist in bukkit
     */
    public World getBukkitWorld(SrpWorld srpWorld) {
        World world = Bukkit.getWorld(srpWorld.getBukkitName());
        if (world == null) {
            throw new WorldMissingException("Missing world " + srpWorld.getBukkitName() + "!");
        }
        return world;
    }
}

