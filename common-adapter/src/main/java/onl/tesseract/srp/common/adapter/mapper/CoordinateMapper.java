package onl.tesseract.srp.common.adapter.mapper;

import onl.tesseract.srp.common.domain.model.Coordinate;
import org.bukkit.Location;

/**
 * Converts Bukkit Location objects to domain Coordinate objects.
 */
public final class CoordinateMapper {
    private CoordinateMapper() {
        // Utility class
    }

    /**
     * Converts a Bukkit Location to a domain Coordinate.
     */
    public static Coordinate toCoordinate(Location location) {
        return new Coordinate(location.getX(), location.getY(), location.getZ(),
                ChunkCoordMapper.toChunkCoord(location));
    }
}
