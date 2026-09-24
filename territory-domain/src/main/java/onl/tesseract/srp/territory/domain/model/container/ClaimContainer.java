package onl.tesseract.srp.territory.domain.model.container;

import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.territory.domain.model.TerritoryChunk;
import onl.tesseract.srp.territory.domain.model.enums.result.ClaimResult;
import onl.tesseract.srp.territory.domain.model.enums.result.UnclaimResult;
import onl.tesseract.srp.territory.domain.model.event.TerritoryClaimEvent;
import onl.tesseract.srp.territory.domain.model.event.TerritoryUnclaimEvent;

import java.util.*;

public abstract class ClaimContainer<TC extends TerritoryChunk> {

    protected final Set<TC> _chunks = new HashSet<>();

    private boolean addChunk(TC chunk) {
        return _chunks.add(chunk);
    }

    public ClaimResult claimChunk(ChunkCoord chunkCoord, UUID claimer) {
        if (!canClaim(claimer)) return ClaimResult.NOT_ALLOWED;
        if (hasChunk(chunkCoord)) return ClaimResult.ALREADY_OWNED;
        if (!hasAdjacent(chunkCoord)) return ClaimResult.NOT_ADJACENT;
        if (addChunk(initChunk(chunkCoord))) return ClaimResult.SUCCESS;
        return ClaimResult.ALREADY_OWNED;
    }

    public boolean hasChunk(ChunkCoord chunkCoord) {
        for (TC chunk : _chunks) {
            if (chunk.getChunkCoord().equals(chunkCoord)) return true;
        }
        return false;
    }

    public boolean hasAdjacent(ChunkCoord chunkCoord) {
        int lx = chunkCoord.x();
        int lz = chunkCoord.z();
        for (TC n : _chunks) {
            int nx = n.getChunkCoord().x();
            int nz = n.getChunkCoord().z();
            if (Math.abs(nx - lx) + Math.abs(nz - lz) == 1) return true;
        }
        return false;
    }

    private boolean removeChunk(TC chunk) {
        return _chunks.remove(chunk);
    }

    public abstract TC initChunk(ChunkCoord chunkCoord);

    public UnclaimResult unclaimChunk(ChunkCoord chunkCoord, UUID player) {
        if (!canClaim(player)) return UnclaimResult.NOT_ALLOWED;
        if (!hasChunk(chunkCoord)) return UnclaimResult.NOT_OWNED;
        if (_chunks.size() == 1) return UnclaimResult.LAST_CHUNK;
        TC chunk = null;
        for (TC c : _chunks) {
            if (c.getChunkCoord().equals(chunkCoord)) {
                chunk = c;
                break;
            }
        }
        if (!isUnclaimStillConnected(chunk)) return UnclaimResult.SPLIT;
        if (removeChunk(chunk)) return UnclaimResult.SUCCESS;
        return UnclaimResult.NOT_OWNED;
    }

    private boolean isUnclaimStillConnected(TC toRemove) {
        List<TC> remaining = new ArrayList<>();
        for (TC chunk : _chunks) {
            if (!chunk.equals(toRemove)) remaining.add(chunk);
        }
        if (remaining.isEmpty()) return false;

        Set<TC> visited = new HashSet<>();
        ArrayDeque<TC> queue = new ArrayDeque<>();
        visited.add(remaining.getFirst());
        queue.add(remaining.getFirst());

        while (!queue.isEmpty()) {
            TC cur = queue.pollFirst();
            for (TC it : remaining) {
                if (!visited.contains(it) && neighbors(cur, it)) {
                    visited.add(it);
                    queue.add(it);
                }
            }
        }
        return visited.size() == remaining.size();
    }

    private boolean neighbors(TC a, TC b) {
        int x1 = a.getChunkCoord().x();
        int z1 = a.getChunkCoord().z();
        int x2 = b.getChunkCoord().x();
        int z2 = b.getChunkCoord().z();
        return Math.abs(x1 - x2) + Math.abs(z1 - z2) == 1;
    }

    public Set<TC> getChunks() {
        return new HashSet<>(_chunks);
    }

    public void addChunks(Collection<TC> toAdd) {
        _chunks.addAll(toAdd);
    }

    public abstract boolean canClaim(UUID player);

    public abstract TerritoryClaimEvent<TC> createClaimEvent(UUID player);

    public abstract TerritoryUnclaimEvent<TC> createUnclaimEvent(UUID player);
}

