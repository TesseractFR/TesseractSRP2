package onl.tesseract.srp.territory.domain.port.userside.campement;


import onl.tesseract.srp.common.domain.port.userside.plugin.EventPublisher;
import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.common.domain.model.Coordinate;
import onl.tesseract.srp.common.domain.model.enums.InteractionAllowResult;
import onl.tesseract.srp.territory.domain.model.TerritoryChunk;
import onl.tesseract.srp.territory.domain.model.campement.Campement;
import onl.tesseract.srp.territory.domain.model.campement.CampementChunk;
import onl.tesseract.srp.territory.domain.model.enums.TerritoryWorld;
import onl.tesseract.srp.territory.domain.model.enums.result.CreationResult;
import onl.tesseract.srp.territory.domain.port.serverside.CampementRepository;
import onl.tesseract.srp.territory.domain.port.serverside.PlayerCampementMessagingRepository;
import onl.tesseract.srp.territory.domain.port.serverside.SrpPlayerRepository;
import onl.tesseract.srp.territory.domain.port.serverside.TerritoryChunkRepository;
import onl.tesseract.srp.territory.domain.port.userside.TerritoryService;

import java.util.List;
import java.util.UUID;

public class CampementService extends TerritoryService<CampementChunk, Campement> {

    private final CampementRepository territoryRepository;
    private final TerritoryChunkRepository territoryChunkRepository;
    private final EventPublisher eventService;
    private final SrpPlayerRepository srpPlayerService;
    private final PlayerCampementMessagingRepository messaging;

    public CampementService(
            CampementRepository territoryRepository,
            TerritoryChunkRepository territoryChunkRepository,
            EventPublisher eventService,
            SrpPlayerRepository srpPlayerService,
            PlayerCampementMessagingRepository messaging) {
        super();
        this.territoryRepository = territoryRepository;
        this.territoryChunkRepository = territoryChunkRepository;
        this.eventService = eventService;
        this.srpPlayerService = srpPlayerService;
        this.messaging = messaging;
    }

    @Override
    protected EventPublisher getEventService() {
        return eventService;
    }

    @Override
    protected TerritoryChunkRepository getTerritoryChunkRepository() {
        return territoryChunkRepository;
    }

    @Override
    protected CampementRepository getTerritoryRepository() {
        return territoryRepository;
    }

    @Override
    public boolean isCorrectWorld(TerritoryWorld world) {
        return world == TerritoryWorld.ELYSEA;
    }

    @Override
    public Campement getByChunk(ChunkCoord chunkCoord) {
        TerritoryChunk territoryChunk = territoryChunkRepository.getById(chunkCoord);
        if (territoryChunk == null) return null;
        var owner = territoryChunk.getOwner();
        if (!(owner instanceof Campement)) return null;
        return (Campement) owner;
    }

    @Override
    protected InteractionAllowResult interactionOutcomeWhenNoOwner() {
        return InteractionAllowResult.Deny;
    }

    @Override
    protected boolean isMemberOrTrusted(Campement territory, UUID playerId) {
        return territory.getOwnerID().equals(playerId) || territory.isTrusted(playerId);
    }

    public Campement getCampementByOwner(UUID ownerID) {
        return territoryRepository.getById(ownerID);
    }

    public List<Campement> getAllCampements() {
        return territoryRepository.findAll();
    }

    public void createCampement(UUID ownerID, Coordinate spawnLocation) {
        CreationResult result = isCreationAvailable(ownerID, spawnLocation.chunkCoord());
        if (result != CreationResult.SUCCESS) {
            switch (result) {
                case INVALID_WORLD -> messaging.playerInvalidWorld(ownerID);
                case NEAR_SPAWN -> messaging.playerNearSpawnCreateCampement(ownerID);
                case TOO_CLOSE_TO_OTHER_TERRITORY -> messaging.playerTooCloseCreateCampement(ownerID);
                case ALREADY_HAS_TERRITORY -> messaging.playerAlreadyHasCampement(ownerID);
                case ON_OTHER_TERRITORY -> messaging.playerOnOtherTerritoryCreateCampement(ownerID);
                default -> { /* Rien */ }
            }
            return;
        }
        int campLevel = srpPlayerService.getCampLevel(ownerID);
        Campement campement = new Campement(ownerID, campLevel, spawnLocation);
        campement.claimInitialChunks();
        territoryRepository.save(campement);
        messaging.playerCampementCreated(ownerID);
    }

    public boolean hasCampement(UUID player) {
        if (getCampementByOwner(player) == null) {
            messaging.playerNoCampement(player);
            return false;
        }
        return true;
    }

    public void deleteOwnCampement(UUID player) {
        territoryRepository.deleteById(player);
        messaging.playerCampementDeleted(player);
    }

    public Coordinate getCampSpawn(UUID ownerID) {
        Campement campement = territoryRepository.getById(ownerID);
        if (campement == null) return null;
        return campement.getSpawnpoint();
    }

    public Coordinate resolveSpawnDestination(UUID player, UUID targetOwner) {
        boolean own = targetOwner == null || targetOwner.equals(player);
        UUID owner = own ? player : targetOwner;
        if (own && getCampementByOwner(player) == null) {
            messaging.playerNoCampement(player);
            return null;
        }
        Coordinate loc = getCampSpawn(owner);
        if (loc == null) {
            if (own) messaging.playerNoSpawnDefined(player);
            else messaging.playerTargetNoCampement(player, owner);
            return null;
        }
        return loc;
    }

    public void notifyTeleported(UUID player, UUID targetOwner) {
        if (targetOwner == null || targetOwner.equals(player)) messaging.playerTeleportedToOwnCampement(player);
        else messaging.playerTeleportedToCampement(player, targetOwner);
    }

    public void defineSpawn(UUID player, Coordinate coordinate) {
        switch (setSpawnpoint(player, coordinate)) {
            case SUCCESS -> messaging.playerSpawnSet(player);
            case NOT_ALLOWED -> messaging.playerNotAllowed(player);
            case OUTSIDE_TERRITORY -> messaging.playerSpawnOutsideTerritory(player);
            case TERRITORY_NOT_FOUND -> messaging.playerNoCampement(player);
        }
    }

    public void claim(UUID player, ChunkCoord chunk) {
        switch (claimChunk(player, chunk)) {
            case SUCCESS -> messaging.playerChunkClaimed(player, chunk.x(), chunk.z());
            case ALREADY_OWNED -> messaging.playerChunkAlreadyOwned(player);
            case ALREADY_OTHER -> messaging.playerChunkOwnedByOther(player);
            case NOT_ADJACENT -> messaging.playerChunkNotAdjacent(player);
            case TOO_CLOSE -> messaging.playerChunkTooClose(player);
            case NOT_ALLOWED -> messaging.playerNotAllowed(player);
            case TERRITORY_NOT_FOUND -> messaging.playerNoCampement(player);
            case INVALID_WORLD -> messaging.playerInvalidWorld(player);
        }
    }

    public void unclaim(UUID player, ChunkCoord chunk) {
        switch (unclaimChunk(player, chunk)) {
            case SUCCESS -> messaging.playerChunkUnclaimed(player, chunk.x(), chunk.z());
            case NOT_OWNED -> messaging.playerChunkNotOwned(player);
            case NOT_ALLOWED -> messaging.playerNotAllowed(player);
            case LAST_CHUNK -> messaging.playerLastChunk(player);
            case IS_SPAWN_CHUNK -> messaging.playerChunkIsSpawn(player);
            case SPLIT -> messaging.playerChunkSplit(player);
            case TERRITORY_NOT_FOUND -> messaging.playerNoCampement(player);
        }
    }

    public void trustPlayer(UUID owner, UUID target) {
        if (owner.equals(target)) { messaging.playerCannotTrustSelf(owner); return; }
        switch (trust(owner, target)) {
            case SUCCESS -> messaging.playerTrusted(owner, target);
            case ALREADY_TRUST -> messaging.playerAlreadyTrusted(owner);
            case NOT_ALLOWED -> messaging.playerNotAllowed(owner);
            case TERRITORY_NOT_FOUND -> messaging.playerNoCampement(owner);
        }
    }

    public void untrustPlayer(UUID owner, UUID target) {
        switch (untrust(owner, target)) {
            case SUCCESS -> messaging.playerUntrusted(owner, target);
            case NOT_TRUST -> messaging.playerNotTrusted(owner);
            case NOT_ALLOWED -> messaging.playerNotAllowed(owner);
            case TERRITORY_NOT_FOUND -> messaging.playerNoCampement(owner);
        }
    }

    /**
     * Increments the level of the player's camp.
     *
     * @param ownerID The UUID of the player who owns the camp.
     */
    public void setCampLevel(UUID ownerID, int level) {
        if (level < 1) throw new IllegalArgumentException("Camp level must be at least 1");
        Campement campement = territoryRepository.getById(ownerID);
        if (campement == null) {
            throw new IllegalArgumentException("Campement from " + ownerID + " does not exist");
        }

        if (campement.getCampLevel() != level) {
            campement.setCampLevel(level);
            territoryRepository.save(campement);
        }
    }

    public void createCampementAsStaff(UUID staff, UUID owner, Coordinate loc) {
        CreationResult result = isCreationAvailable(owner, loc.chunkCoord());
        if (result != CreationResult.SUCCESS) {
            switch (result) {
                case ALREADY_HAS_TERRITORY -> messaging.staffCreateAlreadyHasCampement(staff);
                case INVALID_WORLD -> messaging.staffCreateInvalidWorld(staff);
                case NEAR_SPAWN -> messaging.staffCreateNearSpawn(staff);
                case TOO_CLOSE_TO_OTHER_TERRITORY -> messaging.staffCreateTooClose(staff);
                case ON_OTHER_TERRITORY -> messaging.staffCreateOnOtherTerritory(staff);
                default -> { /* Rien */ }
            }
            return;
        }
        Campement campement = new Campement(owner, srpPlayerService.getCampLevel(owner), loc);
        campement.claimInitialChunks();
        territoryRepository.save(campement);
        var chunk = loc.chunkCoord();
        messaging.staffCampementCreated(staff, owner, chunk.x(), chunk.z());
        messaging.campementCreatedByStaff(owner, chunk.x(), chunk.z());
    }

    public boolean canDeleteCampementAsStaff(UUID staff, UUID owner) {
        if (getCampementByOwner(owner) == null) {
            messaging.staffTargetNoCampement(staff, owner);
            return false;
        }
        return true;
    }

    public void deleteCampementAsStaff(UUID staff, UUID owner) {
        territoryRepository.deleteById(owner);
        messaging.staffCampementDeleted(staff, owner);
        messaging.campementDeletedByStaff(owner);
    }

    public void trustAsStaff(UUID staff, UUID owner, UUID target) {
        if (owner.equals(target)) {
            messaging.staffCannotTrustOwner(staff);
            return;
        }
        switch (trust(owner, target)) {
            case SUCCESS -> {
                messaging.staffTrusted(staff, owner, target);
                messaging.trustedByStaff(target, owner);
            }
            case ALREADY_TRUST -> messaging.staffAlreadyTrusted(staff);
            case NOT_ALLOWED -> messaging.staffNotAllowed(staff);
            case TERRITORY_NOT_FOUND -> messaging.staffTargetNoCampement(staff, owner);
        }
    }

    public void untrustAsStaff(UUID staff, UUID owner, UUID target) {
        switch (untrust(owner, target)) {
            case SUCCESS -> messaging.staffUntrusted(staff, owner, target);
            case NOT_TRUST -> messaging.staffNotTrusted(staff);
            case NOT_ALLOWED -> messaging.staffNotAllowed(staff);
            case TERRITORY_NOT_FOUND -> messaging.staffTargetNoCampement(staff, owner);
        }
    }

    public void showTrustedPlayers(UUID staff, UUID owner) {
        var campement = getCampementByOwner(owner);
        if (campement == null) {
            messaging.staffTargetNoCampement(staff, owner);
            return;
        }
        messaging.staffTrustedList(staff, owner, campement.getTrusted());
    }
}

