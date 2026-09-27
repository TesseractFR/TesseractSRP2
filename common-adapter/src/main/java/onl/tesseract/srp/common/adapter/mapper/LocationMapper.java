package onl.tesseract.srp.common.adapter.mapper;

import onl.tesseract.srp.common.domain.model.Coordinate;
import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * Converts domain Coordinate objects to Bukkit Location objects.
 */
public final class LocationMapper {
    private LocationMapper() {
        // Utility class
    }

    /**
     * Converts a domain Coordinate to a Bukkit Location.
     */
    public static Location toLocation(Coordinate coordinate) {
        var world = Bukkit.getWorld(coordinate.chunkCoord().world());
        return new Location(world, coordinate.x(), coordinate.y(), coordinate.z());
    }
}

