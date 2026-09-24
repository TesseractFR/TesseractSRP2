package onl.tesseract.srp.territory.adapter.serverside.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.common.domain.model.Coordinate;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class CoordinateEntity {

    private static final int CHUNK_SIZE = 16;

    private double x;
    private double y;
    private double z;
    private String world;

    public CoordinateEntity(Coordinate coordinate) {
        this.x = coordinate.x();
        this.y = coordinate.y();
        this.z = coordinate.z();
        this.world = coordinate.chunkCoord().world();
    }

    public Coordinate toDomain() {
        ChunkCoord chunkCoord = new ChunkCoord(
                (int) Math.floor(x/CHUNK_SIZE),
                (int) Math.floor(z/CHUNK_SIZE),
                world
        );
        return new Coordinate(x, y, z, chunkCoord);
    }
}

