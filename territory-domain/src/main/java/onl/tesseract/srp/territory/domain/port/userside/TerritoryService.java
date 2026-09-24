package onl.tesseract.srp.territory.domain.port.userside;

import onl.tesseract.srp.common.domain.EventPublisher;
import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.common.domain.model.Coordinate;
import onl.tesseract.srp.common.domain.model.enums.InteractionAllowResult;
import onl.tesseract.srp.territory.domain.model.Territory;
import onl.tesseract.srp.territory.domain.model.TerritoryChunk;
import onl.tesseract.srp.territory.domain.model.enums.TerritoryWorld;
import onl.tesseract.srp.territory.domain.model.enums.result.ClaimResult;
import onl.tesseract.srp.territory.domain.model.enums.result.CreationResult;
import onl.tesseract.srp.territory.domain.model.enums.result.SetSpawnResult;
import onl.tesseract.srp.territory.domain.model.enums.result.TrustResult;
import onl.tesseract.srp.territory.domain.model.enums.result.UnclaimResult;
import onl.tesseract.srp.territory.domain.model.enums.result.UntrustResult;
import onl.tesseract.srp.territory.domain.port.serverside.TerritoryChunkRepository;
import onl.tesseract.srp.territory.domain.port.serverside.TerritoryRepository;

import java.util.Collection;
import java.util.UUID;

public abstract class TerritoryService<TC extends TerritoryChunk, T extends Territory<TC>> {

    protected abstract EventPublisher getEventService();
    protected abstract TerritoryChunkRepository getTerritoryChunkRepository();
    protected abstract TerritoryRepository<T, UUID> getTerritoryRepository();

    /**
     * Permet de savoir si le monde est correct pour une location donnée.
     * @param worldName Le nom du monde à valider
     */
    public boolean isCorrectWorld(String worldName) {
        TerritoryWorld territoryWorld = getTerritoryWorld(worldName);
        if (territoryWorld == null) {
            return false;
        }
        return isCorrectWorld(territoryWorld);
    }

    public abstract boolean isCorrectWorld(TerritoryWorld world);

    private TerritoryWorld getTerritoryWorld(String world) {
        for (TerritoryWorld entry : TerritoryWorld.values()) {
            if (entry.getSrpWorld().getBukkitName().equals(world)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Permet de savoir si un chunk est déjà occupé par un territoire.
     */
    protected boolean isChunkTaken(ChunkCoord chunkCoord) {
        return getTerritoryChunkRepository().getById(chunkCoord) != null;
    }

    /**
     * Permet de savoir si un chunk est déjà occupé par un autre.
     */
    protected boolean isTakenByOther(T territory, ChunkCoord chunkCoord) {
        TerritoryChunk territoryChunk = getTerritoryChunkRepository().getById(chunkCoord);
        if (territoryChunk == null) {
            return false;
        }
        return territory.equals(territoryChunk.getOwner());
    }

    /**
     * Retourne le territoire qui possède ce chunkCoord.
     */
    public abstract T getByChunk(ChunkCoord chunkCoord);

    /**
     * Retourne le territoire qui possède ce chunkCoord.
     */
    public Territory<? extends TerritoryChunk> getAnyByChunk(ChunkCoord chunkCoord) {
        TerritoryChunk territoryChunk = getTerritoryChunkRepository().getById(chunkCoord);
        if (territoryChunk == null) {
            return null;
        }
        return territoryChunk.getOwner();
    }

    /**
     * Retourne le territoire lié au joueur.
     */
    public T getByPlayer(UUID player) {
        return getTerritoryRepository().findByPlayer(player);
    }

    /**
     * Permet de claim un chunk
     */
    public ClaimResult claimChunk(UUID player, ChunkCoord chunkCoord) {
        TerritoryWorld world = getTerritoryWorld(chunkCoord.world());
        if (world == null || !isCorrectWorld(world)) {
            return ClaimResult.INVALID_WORLD;
        }
        T territory = getByPlayer(player);
        if (territory == null) {
            return ClaimResult.TERRITORY_NOT_FOUND;
        }
        Territory<?> ownerTerritory = getAnyByChunk(chunkCoord);
        if (ownerTerritory != null) {
            if (!ownerTerritory.equals(territory)) {
                return ClaimResult.ALREADY_OTHER;
            }
            return ClaimResult.ALREADY_OWNED;
        }
        boolean tooClose = isTooCloseToOthers(world, chunkCoord.x(), chunkCoord.z(), territory);
        if (tooClose) {
            return ClaimResult.TOO_CLOSE;
        }
        ClaimResult result = territory.claimChunk(chunkCoord, player);
        if (result == ClaimResult.SUCCESS) {
            getTerritoryRepository().save(territory);
            getEventService().publish(territory.createClaimEvent(player));
        }
        return result;
    }

    /**
     * Permet de unclaim un chunk
     */
    public UnclaimResult unclaimChunk(UUID player, ChunkCoord chunkCoord) {
        T territory = getByPlayer(player);
        if (territory == null) {
            return UnclaimResult.TERRITORY_NOT_FOUND;
        }
        UnclaimResult result = territory.unclaimChunk(chunkCoord, player);
        if (result == UnclaimResult.SUCCESS) {
            getTerritoryRepository().save(territory);
            getEventService().publish(territory.createUnclaimEvent(player));
        }
        return result;
    }

    /**
     * Permet de set le point de spawn
     */
    public SetSpawnResult setSpawnpoint(UUID player, Coordinate coordinate) {
        T territory = getByPlayer(player);
        if (territory == null) {
            return SetSpawnResult.TERRITORY_NOT_FOUND;
        }
        SetSpawnResult result = territory.setSpawnpoint(coordinate, player);
        if (result == SetSpawnResult.SUCCESS) {
            getTerritoryRepository().save(territory);
        }
        return result;
    }

    protected abstract InteractionAllowResult interactionOutcomeWhenNoOwner();

    protected abstract boolean isMemberOrTrusted(T territory, UUID playerId);

    private boolean isTooCloseToOthers(TerritoryWorld world, int x0, int z0, T territory) {
        Collection<TerritoryChunk> alreadyClaimed = getTerritoryChunkRepository().findAllByRange(
                world.getSrpWorld().getBukkitName(),
                x0 - world.getClaimDistance(),
                x0 + world.getClaimDistance(),
                z0 - world.getClaimDistance(),
                z0 + world.getClaimDistance());
        return alreadyClaimed.stream()
                .anyMatch(chunk -> !chunk.getOwner().equals(territory));
    }

    public InteractionAllowResult canInteractInChunk(UUID playerId, ChunkCoord chunk) {
        if (!isCorrectWorld(chunk.world())) {
            return InteractionAllowResult.Ignore;
        }
        T owner = getByChunk(chunk);
        if (owner != null) {
            if (owner.canBuild(playerId)) {
                return InteractionAllowResult.Allow;
            } else {
                return InteractionAllowResult.Deny;
            }
        }
        return interactionOutcomeWhenNoOwner();
    }

    /**
     * Méthode core permettant de savoir si la création d'un territoire est possible
     */
    protected CreationResult isCreationAvailable(UUID playerID, ChunkCoord chunkCoord) {
        if (getByPlayer(playerID) != null) {
            return CreationResult.ALREADY_HAS_TERRITORY;
        }
        TerritoryWorld world = getTerritoryWorld(chunkCoord.world());
        if (world == null || !isCorrectWorld(world)) {
            return CreationResult.INVALID_WORLD;
        }
        if (isChunkTaken(chunkCoord)) {
            return CreationResult.ON_OTHER_TERRITORY;
        }
        int x = chunkCoord.x();
        int z = chunkCoord.z();
        Collection<TerritoryChunk> alreadyClaimed = getTerritoryChunkRepository().findAllByRange(
                chunkCoord.world(),
                x - world.getCreationDistance(),
                x + world.getCreationDistance(),
                z - world.getCreationDistance(),
                z + world.getCreationDistance());
        if (!alreadyClaimed.isEmpty()) {
            return CreationResult.TOO_CLOSE_TO_OTHER_TERRITORY;
        }
        return CreationResult.SUCCESS;
    }

    public T getById(UUID id) {
        return getTerritoryRepository().getById(id);
    }

    /**
     * Permet de trust un joueur
     */
    public TrustResult trust(UUID player, UUID target) {
        T territory = getTerritoryRepository().findByPlayer(player);
        if (territory == null) {
            return TrustResult.TERRITORY_NOT_FOUND;
        }
        if (!territory.canTrust(player)) {
            return TrustResult.NOT_ALLOWED;
        }
        TrustResult result = territory.addTrust(player, target);
        if (result == TrustResult.SUCCESS) {
            getTerritoryRepository().save(territory);
        }
        return result;
    }

    /**
     * Permet de untrust un joueur
     */
    public UntrustResult untrust(UUID player, UUID target) {
        T territory = getTerritoryRepository().findByPlayer(player);
        if (territory == null) {
            return UntrustResult.TERRITORY_NOT_FOUND;
        }
        if (!territory.canTrust(player)) {
            return UntrustResult.NOT_ALLOWED;
        }
        UntrustResult result = territory.removeTrust(player, target);
        if (result == UntrustResult.SUCCESS) {
            getTerritoryRepository().save(territory);
        }
        return result;
    }
}

