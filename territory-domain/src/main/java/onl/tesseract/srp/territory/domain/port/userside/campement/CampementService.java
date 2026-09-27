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

    public CampementService(
            CampementRepository territoryRepository,
            TerritoryChunkRepository territoryChunkRepository,
            EventPublisher eventService,
            SrpPlayerRepository srpPlayerService) {
        this.territoryRepository = territoryRepository;
        this.territoryChunkRepository = territoryChunkRepository;
        this.eventService = eventService;
        this.srpPlayerService = srpPlayerService;
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

    public CreationResult createCampement(UUID ownerID, Coordinate spawnLocation) {
        CreationResult result = isCreationAvailable(ownerID, spawnLocation.chunkCoord());
        if (result != CreationResult.SUCCESS) return result;

        int campLevel = srpPlayerService.getCampLevel();
        Campement campement = new Campement(ownerID, campLevel, spawnLocation);
        campement.claimInitialChunks();
        territoryRepository.save(campement);
        return result;
    }

    public void deleteCampement(UUID id) {
        territoryRepository.deleteById(id);
    }

    private Campement getByOwner(UUID ownerID) {
        return territoryRepository.getById(ownerID);
    }

    public Coordinate getCampSpawn(UUID ownerID) {
        Campement campement = territoryRepository.getById(ownerID);
        if (campement == null) return null;
        return campement.getSpawnpoint();
    }

    /**
     * Increments the level of the player's camp.
     * @param ownerID The UUID of the player who owns the camp.
     * @return The new camp level if successful, or null if the camp does not exist.
     */
    public Boolean setCampLevel(UUID ownerID, int level) {
        Campement campement = territoryRepository.getById(ownerID);
        if (campement == null) {
            throw new IllegalArgumentException("Campement from " + ownerID + " does not exist");
        }

        if (campement.getCampLevel() != level) {
            campement.setCampLevel(level);
            territoryRepository.save(campement);
            return true;
        }
        return false;
    }
}

