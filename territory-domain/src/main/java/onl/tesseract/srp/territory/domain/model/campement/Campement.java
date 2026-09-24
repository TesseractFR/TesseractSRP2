package onl.tesseract.srp.territory.domain.model.campement;

import lombok.Getter;
import lombok.Setter;
import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.common.domain.model.Coordinate;
import onl.tesseract.srp.territory.domain.model.Territory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class Campement extends Territory<CampementChunk> {
    private final UUID ownerID;
    @Setter
    private int campLevel;

    public Campement(UUID ownerID, int campLevel, Coordinate spawnLocation) {
        this(ownerID, campLevel, spawnLocation, new HashSet<>());
    }

    public Campement(UUID ownerID, int campLevel, Coordinate spawnLocation, Set<UUID> trustedPlayers) {
        super(ownerID, spawnLocation, trustedPlayers);
        this.ownerID = ownerID;
        this.campLevel = campLevel;
    }

    @Override
    public CampementChunk initChunk(ChunkCoord chunkCoord) {
        return new CampementChunk(chunkCoord, this);
    }

    @Override
    public boolean canClaim(UUID player) {
        return player.equals(ownerID);
    }

    @Override
    public boolean canTrust(UUID player) {
        return player.equals(ownerID);
    }

    @Override
    public CampementChunkClaimEvent createClaimEvent(UUID player) {
        return new CampementChunkClaimEvent(player);
    }

    @Override
    public CampementChunkUnclaimEvent createUnclaimEvent(UUID player) {
        return new CampementChunkUnclaimEvent(player);
    }

    @Override
    public void claimInitialChunks() {
        _chunks.add(new CampementChunk(getSpawnContainer().getSpawnpoint().chunkCoord(), this));
    }

    @Override
    public boolean canSetSpawn(UUID player) {
        return player.equals(ownerID);
    }

    @Override
    public boolean canBuild(UUID player) {
        return player.equals(ownerID) || isTrusted(player);
    }

    @Override
    public boolean canOpenChest(UUID player) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

