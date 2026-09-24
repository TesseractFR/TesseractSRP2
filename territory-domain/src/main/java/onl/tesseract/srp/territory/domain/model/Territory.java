package onl.tesseract.srp.territory.domain.model;

import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.common.domain.model.Coordinate;
import onl.tesseract.srp.territory.domain.model.container.ClaimContainer;
import onl.tesseract.srp.territory.domain.model.container.DefaultSpawnContainer;
import onl.tesseract.srp.territory.domain.model.container.DefaultTrustContainer;
import onl.tesseract.srp.territory.domain.model.container.SpawnContainer;
import onl.tesseract.srp.territory.domain.model.container.TrustContainer;
import onl.tesseract.srp.territory.domain.model.enums.result.SetSpawnResult;
import onl.tesseract.srp.territory.domain.model.enums.result.TrustResult;
import onl.tesseract.srp.territory.domain.model.enums.result.UnclaimResult;
import onl.tesseract.srp.territory.domain.model.enums.result.UntrustResult;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public abstract class Territory<TC extends TerritoryChunk> extends ClaimContainer<TC> implements SpawnContainer, TrustContainer {

    private final UUID id;
    private final SpawnContainer spawnContainer;
    private final TrustContainer trustContainer;

    public Territory(UUID id, Coordinate spawnLocation, Set<UUID> trustedPlayers) {
        this(id, new DefaultSpawnContainer(spawnLocation), new DefaultTrustContainer(trustedPlayers));
    }

    public Territory(UUID id, SpawnContainer spawnContainer, TrustContainer trustContainer) {
        this.id = id;
        this.spawnContainer = spawnContainer;
        this.trustContainer = trustContainer;
    }

    // --- SpawnContainer delegation ---

    @Override
    public SetSpawnResult setSpawnpoint(Coordinate coordinate, UUID player) {
        if (!canSetSpawn(player)) return SetSpawnResult.NOT_ALLOWED;
        if (!hasChunk(coordinate.chunkCoord())) return SetSpawnResult.OUTSIDE_TERRITORY;
        return spawnContainer.setSpawnpoint(coordinate, player);
    }

    @Override
    public boolean isSpawnChunk(ChunkCoord chunkCoord) {
        return spawnContainer.isSpawnChunk(chunkCoord);
    }

    @Override
    public boolean canSetSpawn(UUID player) {
        return spawnContainer.canSetSpawn(player);
    }

    @Override
    public Coordinate getSpawnpoint() {
        return spawnContainer.getSpawnpoint();
    }

    // --- TrustContainer delegation ---

    @Override
    public TrustResult addTrust(UUID player, UUID target) {
        return trustContainer.addTrust(player, target);
    }

    @Override
    public UntrustResult removeTrust(UUID player, UUID target) {
        return trustContainer.removeTrust(player, target);
    }

    @Override
    public boolean isTrusted(UUID player) {
        return trustContainer.isTrusted(player);
    }

    @Override
    public boolean canTrust(UUID player) {
        return trustContainer.canTrust(player);
    }

    @Override
    public Collection<UUID> getTrusted() {
        return trustContainer.getTrusted();
    }

    // --- ClaimContainer override ---

    @Override
    public UnclaimResult unclaimChunk(ChunkCoord chunkCoord, UUID player) {
        if (isSpawnChunk(chunkCoord)) return UnclaimResult.IS_SPAWN_CHUNK;
        return super.unclaimChunk(chunkCoord, player);
    }

    // --- Abstract methods ---

    public abstract boolean canBuild(UUID player);

    public abstract boolean canOpenChest(UUID player);

    public abstract void claimInitialChunks();

    // --- Getters ---

    public UUID getId() {
        return id;
    }

    public SpawnContainer getSpawnContainer() {
        return spawnContainer;
    }

    public TrustContainer getTrustContainer() {
        return trustContainer;
    }

    // --- equals & hashCode ---

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || this.getClass() != other.getClass()) return false;
        Territory<?> that = (Territory<?>) other;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + this.getClass().hashCode();
        return result;
    }
}

