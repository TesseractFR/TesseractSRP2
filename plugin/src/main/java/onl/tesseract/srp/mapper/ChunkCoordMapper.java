package onl.tesseract.srp.mapper;

import onl.tesseract.srp.common.domain.model.ChunkCoord;
import org.bukkit.Chunk;
import org.bukkit.Location;

/**
 * Converts Bukkit Chunk/Location objects to domain ChunkCoord objects.
 */
public final class ChunkCoordMapper {
    private ChunkCoordMapper() {
        // Utility class
    }

    /**
     * Converts a Bukkit Location to a domain ChunkCoord.
     */
    public static ChunkCoord toChunkCoord(Location location) {
        return new ChunkCoord(location.getBlockX() >> 4, location.getBlockZ() >> 4, location.getWorld().getName());
    }

    /**
     * Converts a Bukkit Chunk to a domain ChunkCoord.
     */
    public static ChunkCoord toChunkCoord(Chunk chunk) {
        return new ChunkCoord(chunk.getX(), chunk.getZ(), chunk.getWorld().getName());
    }
}

