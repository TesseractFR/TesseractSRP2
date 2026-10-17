package onl.tesseract.srp.territory.domain.port.serverside;

import java.util.UUID;

public interface PlayerCampementMessagingRepository {
    // communs
    void playerNoCampement(UUID player);
    void playerNotAllowed(UUID player);
    void playerInvalidWorld(UUID player);

    // create
    void playerNearSpawnCreateCampement(UUID player);
    void playerTooCloseCreateCampement(UUID player);
    void playerAlreadyHasCampement(UUID player);
    void playerOnOtherTerritoryCreateCampement(UUID player);
    void playerCampementCreated(UUID player);

    // delete
    void playerCampementDeleted(UUID player);

    // spawn
    void playerNoSpawnDefined(UUID player);
    void playerTargetNoCampement(UUID player, UUID target);
    void playerTeleportedToOwnCampement(UUID player);
    void playerTeleportedToCampement(UUID player, UUID owner);
    void playerSpawnSet(UUID player);
    void playerSpawnOutsideTerritory(UUID player);

    // claim / unclaim
    void playerChunkClaimed(UUID player, int x, int z);
    void playerChunkAlreadyOwned(UUID player);
    void playerChunkOwnedByOther(UUID player);
    void playerChunkNotAdjacent(UUID player);
    void playerChunkTooClose(UUID player);
    void playerChunkUnclaimed(UUID player, int x, int z);
    void playerChunkNotOwned(UUID player);
    void playerLastChunk(UUID player);
    void playerChunkIsSpawn(UUID player);
    void playerChunkSplit(UUID player);

    // trust / untrust
    void playerCannotTrustSelf(UUID player);
    void playerTrusted(UUID owner, UUID target);
    void playerAlreadyTrusted(UUID owner);
    void playerUntrusted(UUID owner, UUID target);
    void playerNotTrusted(UUID owner);

    // border
    void playerBordersShown(UUID player);
    void playerBordersHidden(UUID player);

    // staff
    void staffNotAllowed(UUID staff);
    void staffTargetNoCampement(UUID staff, UUID owner);
    void staffCampementCreated(UUID staff, UUID owner, int chunkX, int chunkZ);
    void campementCreatedByStaff(UUID owner, int chunkX, int chunkZ);
    void staffCreateAlreadyHasCampement(UUID staff);
    void staffCreateInvalidWorld(UUID staff);
    void staffCreateNearSpawn(UUID staff);
    void staffCreateTooClose(UUID staff);
    void staffCreateOnOtherTerritory(UUID staff);
    void staffCampementDeleted(UUID staff, UUID owner);
    void campementDeletedByStaff(UUID owner);
    void staffCannotTrustOwner(UUID staff);
    void staffTrusted(UUID staff, UUID owner, UUID target);
    void trustedByStaff(UUID target, UUID owner);
    void staffAlreadyTrusted(UUID staff);
    void staffUntrusted(UUID staff, UUID owner, UUID target);
    void staffNotTrusted(UUID staff);
    void staffTrustedList(UUID staff, UUID owner, java.util.Collection<UUID> trusted);
}