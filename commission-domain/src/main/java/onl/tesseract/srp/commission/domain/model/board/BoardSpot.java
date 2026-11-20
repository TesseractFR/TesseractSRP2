package onl.tesseract.srp.commission.domain.model.board;

import lombok.NonNull;
import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.common.domain.model.Coordinate;

import java.util.HashSet;
import java.util.Set;

/**
 * Emplacement choisi pour poser un tableau de guilde.
 *
 * @param centerBlock bloc du milieu du tableau, qui fait trois blocs de large
 * @param facing      orientation du tableau
 */
public record BoardSpot(@NonNull Coordinate centerBlock, @NonNull BoardFacing facing) {
    /** Nombre de blocs de part et d'autre du bloc central. */
    public static final int HALF_WIDTH = 1;

    /** @return les chunks recouverts par le tableau, qui doivent tous appartenir à la guilde */
    public Set<ChunkCoord> findAllOccupiedChunks() {
        int centerBlockX = (int) Math.floor(centerBlock.x());
        int centerBlockZ = (int) Math.floor(centerBlock.z());
        Set<ChunkCoord> chunks = new HashSet<>();
        for (int offset = -HALF_WIDTH; offset <= HALF_WIDTH; offset++) {
            int blockX = facing.isWidthAlongX() ? centerBlockX + offset : centerBlockX;
            int blockZ = facing.isWidthAlongX() ? centerBlockZ : centerBlockZ + offset;
            chunks.add(new ChunkCoord(blockX >> 4, blockZ >> 4, centerBlock.chunkCoord().world()));
        }
        return Set.copyOf(chunks);
    }
}
