package onl.tesseract.srp.territory.adapter.serverside.infrastructure.runnable;

import onl.tesseract.srp.common.domain.model.ChunkCoord;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Bukkit task that draws territory borders using particles.
 */
public class TerritoryBorderTask extends BukkitRunnable {
    private static final int CHUNK_SIZE = 16;
    private static final int MAX_DISTANCE = 50;
    private static final int MAX_DISTANCE_SQ = MAX_DISTANCE * MAX_DISTANCE;
    private static final int VERTICAL_STEP = 2;
    private static final int HORIZONTAL_STEP = 1;
    private static final Particle PARTICLE = Particle.END_ROD;
    private static final int PARTICLE_COUNT = 1;
    private static final double OFFSET = 0.1;
    private static final double EXTRA = 0.01;

    private final Player player;
    private final Collection<ChunkCoord> chunks;

    public TerritoryBorderTask(Player player, Collection<ChunkCoord> chunks) {
        this.player = player;
        this.chunks = chunks;
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            cancel();
            return;
        }
        drawBordersOnce(player, chunks);
    }

    private void spawnVerticalBorder(int blockX, int startZ, int endZ, int minY, int maxY, Location startLoc) {
        for (int bz = startZ; bz <= endZ; bz += HORIZONTAL_STEP) {
            for (int by = minY; by <= maxY; by += VERTICAL_STEP) {
                Location pos = startLoc.clone();
                pos.setX((double) blockX);
                pos.setY((double) by);
                pos.setZ((double) bz);
                if (pos.distanceSquared(startLoc) <= MAX_DISTANCE_SQ) {
                    player.spawnParticle(PARTICLE, pos, PARTICLE_COUNT, OFFSET, OFFSET, OFFSET, EXTRA);
                }
            }
        }
    }

    private void spawnHorizontalBorder(int startX, int endX, int blockZ, int minY, int maxY, Location startLoc) {
        for (int bx = startX; bx <= endX; bx += HORIZONTAL_STEP) {
            for (int by = minY; by <= maxY; by += VERTICAL_STEP) {
                Location pos = startLoc.clone();
                pos.setX((double) bx);
                pos.setY((double) by);
                pos.setZ((double) blockZ);
                if (pos.distanceSquared(startLoc) <= MAX_DISTANCE_SQ) {
                    player.spawnParticle(PARTICLE, pos, PARTICLE_COUNT, OFFSET, OFFSET, OFFSET, EXTRA);
                }
            }
        }
    }

    /**
     * Draws borders around specified chunks for a player using particles.
     */
    public void drawBordersOnce(Player player, Collection<ChunkCoord> chunks) {
        Set<java.util.AbstractMap.SimpleEntry<Integer, Integer>> set = new HashSet<>();
        for (ChunkCoord chunk : chunks) {
            set.add(new java.util.AbstractMap.SimpleEntry<>(chunk.x(), chunk.z()));
        }

        org.bukkit.World world = player.getWorld();
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight();
        Location ploc = player.getLocation();

        for (java.util.AbstractMap.SimpleEntry<Integer, Integer> entry : set) {
            int cx = entry.getKey();
            int cz = entry.getValue();

            boolean hasLeft = set.contains(new java.util.AbstractMap.SimpleEntry<>(cx - 1, cz));
            boolean hasRight = set.contains(new java.util.AbstractMap.SimpleEntry<>(cx + 1, cz));
            boolean hasTop = set.contains(new java.util.AbstractMap.SimpleEntry<>(cx, cz - 1));
            boolean hasBottom = set.contains(new java.util.AbstractMap.SimpleEntry<>(cx, cz + 1));

            int minX = cx * CHUNK_SIZE;
            int minZ = cz * CHUNK_SIZE;
            int maxX = minX + CHUNK_SIZE;
            int maxZ = minZ + CHUNK_SIZE;

            if (!hasLeft) spawnVerticalBorder(minX, minZ, maxZ, minY, maxY, ploc);
            if (!hasRight) spawnVerticalBorder(maxX, minZ, maxZ, minY, maxY, ploc);
            if (!hasTop) spawnHorizontalBorder(minX, maxX, minZ, minY, maxY, ploc);
            if (!hasBottom) spawnHorizontalBorder(minX, maxX, maxZ, minY, maxY, ploc);
        }
    }
}

