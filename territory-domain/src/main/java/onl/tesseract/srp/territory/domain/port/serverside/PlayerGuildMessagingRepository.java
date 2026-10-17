package onl.tesseract.srp.territory.domain.port.serverside;

import onl.tesseract.srp.territory.domain.model.guild.GuildMember;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRank;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRole;

import java.util.List;
import java.util.UUID;

public interface PlayerGuildMessagingRepository {
    // communs
    void playerNoGuild(UUID player);
    void playerNotAllowed(UUID player);
    void playerInvalidWorld(UUID player);

    // create
    void playerRankTooLowCreateGuild(UUID player);
    void playerNoMoneyCreateGuild(UUID player);
    void playerNameTakenCreateGuild(UUID player);
    void playerNearSpawnCreateGuild(UUID player);
    void playerTooCloseCreateGuild(UUID player);
    void playerAlreadyHasGuildCreateGuild(UUID player);
    void playerOnOtherTerritoryCreateGuild(UUID player);
    void playerGuildCreated(UUID player, String guildName);

    // delete
    void playerGuildDeleted(UUID player);
    void playerDeleteGuildFailed(UUID player);

    // invite
    void playerCannotInviteSelf(UUID player);
    void playerInviteTargetHasGuild(UUID player, UUID target);
    void playerInviteSent(UUID sender, UUID target);
    void playerInviteJoined(UUID sender, UUID target, String guildName);
    void playerJoinGuildNotFound(UUID player, String name);
    void playerAlreadyInGuild(UUID player);
    void playerGuildFull(UUID player);
    void playerAlreadyRequested(UUID player);
    void playerJoinRequestSent(UUID player, String guildName);

    // kick / leave
    void playerKickNotMember(UUID player, UUID target);
    void playerCannotKickLeader(UUID player);
    void playerKicked(UUID sender, UUID target);
    void playerLeaderMustDelete(UUID player);
    void playerLeftGuild(UUID player);

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

    // spawn
    void playerSpawnSet(UUID player, boolean visitor);
    void playerSpawnOutsideTerritory(UUID player);
    void playerNoSpawnDefined(UUID player);
    void playerGuildNotFound(UUID player, String name);
    void playerTeleportedToSpawn(UUID player, String guildName, boolean own);

    // border
    void playerBordersShown(UUID player);
    void playerBordersHidden(UUID player);

    // staff
    void staffGuildDeleted(UUID staff, String guildName);
    void staffDeleteFailed(UUID staff);
    void staffShowMembers(UUID staff, String guildName, List<GuildMember> members);
    void staffMemberAlreadyInGuild(UUID staff, UUID target);
    void staffMemberHasOtherGuild(UUID staff, UUID target);
    void staffMemberAdded(UUID staff, UUID target, String guildName);
    void staffMemberNotInGuild(UUID staff, UUID target);
    void staffCannotKickLeader(UUID staff);
    void staffMemberKicked(UUID staff, UUID target, String guildName);
    void staffPlayerNotFound(UUID staff);
    void staffLeaderSet(UUID staff, String guildName, UUID newLeader, UUID oldLeader);
    void staffRoleSet(UUID staff, UUID target, GuildRole role, String guildName);
    void staffRoleSetWithReplacement(UUID staff, UUID target, GuildRole role, UUID replacement, String guildName);
    void staffNeedNewLeader(UUID staff, String guildName, UUID target, GuildRole role);
    void staffNewLeaderSameAsTarget(UUID staff);
    void staffSameRole(UUID staff, UUID target, GuildRole role, String guildName);

    // staff : opérations génériques
    void staffMoneyGiven(UUID staff, String guildName, int amount);
    void staffMoneyTaken(UUID staff, String guildName, int amount);
    void staffShowMoney(UUID staff, String guildName, int money);
    void staffInvalidAmount(UUID staff);
    void staffNotEnoughGuildMoney(UUID staff, String guildName);
    void staffLevelSet(UUID staff, String guildName, int level);
    void staffLevelAdded(UUID staff, String guildName, int amount);
    void staffShowLevel(UUID staff, String guildName, int level);
    void staffXpAdded(UUID staff, String guildName, int amount);
    void staffRankSet(UUID staff, String guildName, GuildRank rank);
    void staffShowRank(UUID staff, String guildName, GuildRank rank);
}