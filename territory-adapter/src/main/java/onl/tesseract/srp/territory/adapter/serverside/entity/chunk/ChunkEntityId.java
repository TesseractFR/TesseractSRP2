package onl.tesseract.srp.territory.adapter.serverside.entity.chunk;


import jakarta.persistence.Embeddable;
import onl.tesseract.srp.common.domain.model.ChunkCoord;

@Embeddable
public class ChunkEntityId {
    private int x;
    private int z;
    private String world;

    public ChunkEntityId() {
    }

    public ChunkEntityId(int x, int z, String world) {
        this.x = x;
        this.z = z;
        this.world = world;
    }

    public ChunkCoord toDomain() {
        return new ChunkCoord(x, z, world);
    }

    public static ChunkEntityId fromDomain(ChunkCoord chunkCoord) {
        return new ChunkEntityId(chunkCoord.x(), chunkCoord.z(), chunkCoord.world());
    }
}
