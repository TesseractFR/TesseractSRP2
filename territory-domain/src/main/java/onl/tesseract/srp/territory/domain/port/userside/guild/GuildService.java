package onl.tesseract.srp.territory.domain.port.userside.guild;


import onl.tesseract.srp.common.domain.port.userside.plugin.EventPublisher;
import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.common.domain.model.Coordinate;
import onl.tesseract.srp.common.domain.model.enums.InteractionAllowResult;
import onl.tesseract.srp.common.domain.model.enums.StaffSetRoleResult;
import onl.tesseract.srp.territory.domain.model.TerritoryChunk;
import onl.tesseract.srp.territory.domain.model.enums.TerritoryWorld;
import onl.tesseract.srp.territory.domain.model.enums.result.CreationResult;
import onl.tesseract.srp.territory.domain.model.enums.result.KickResult;
import onl.tesseract.srp.territory.domain.model.enums.result.LeaveResult;
import onl.tesseract.srp.territory.domain.model.enums.result.SetSpawnResult;
import onl.tesseract.srp.territory.domain.model.guild.Guild;
import onl.tesseract.srp.territory.domain.model.guild.GuildChunk;
import onl.tesseract.srp.territory.domain.model.guild.enums.*;
import onl.tesseract.srp.territory.domain.model.guild.event.GuildInvitationEvent;
import onl.tesseract.srp.territory.domain.model.guild.event.GuildLevelUpEvent;
import onl.tesseract.srp.territory.domain.port.serverside.GuildRepository;
import onl.tesseract.srp.territory.domain.port.serverside.SrpPlayerRepository;
import onl.tesseract.srp.territory.domain.port.serverside.TerritoryChunkRepository;
import onl.tesseract.srp.territory.domain.port.userside.TerritoryService;

import java.util.Collection;
import java.util.UUID;


public class GuildService extends TerritoryService<GuildChunk, Guild> {
    private static final int GUILD_COST = 10_000;
    private static final int XP_PER_LVL_MULTIPLICATOR = 1000;

    private final GuildRepository territoryRepository;
    private final TerritoryChunkRepository territoryChunkRepository;
    private final SrpPlayerRepository playerService;
    private final EventPublisher eventService;
    private final MoneyService moneyService;

    public GuildService(
            GuildRepository territoryRepository,
            TerritoryChunkRepository territoryChunkRepository,
            SrpPlayerRepository playerService,
            EventPublisher eventService,
            MoneyService moneyService) {
        this.territoryRepository = territoryRepository;
        this.territoryChunkRepository = territoryChunkRepository;
        this.playerService = playerService;
        this.eventService = eventService;
        this.moneyService = moneyService;
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
    protected GuildRepository getTerritoryRepository() {
        return territoryRepository;
    }

    @Override
    public boolean isCorrectWorld(TerritoryWorld world) {
        return world == TerritoryWorld.GUILDWORLD;
    }

    @Override
    public Guild getByChunk(ChunkCoord chunkCoord) {
        TerritoryChunk territoryChunk = territoryChunkRepository.getById(chunkCoord);
        if (territoryChunk == null) return null;
        var owner = territoryChunk.getOwner();
        if (!(owner instanceof Guild)) return null;
        return (Guild) owner;
    }

    @Override
    protected InteractionAllowResult interactionOutcomeWhenNoOwner() {
        return InteractionAllowResult.Ignore;
    }

    @Override
    protected boolean isMemberOrTrusted(Guild territory, UUID playerId) {
        var playerGuild = territoryRepository.findGuildByMember(playerId);
        if (playerGuild == null) return false;
        return playerGuild.getId().equals(territory.getId());
    }

    private Guild getGuild(UUID guildID) {
        var guild = territoryRepository.getById(guildID);
        if (guild == null) {
            throw new IllegalArgumentException("Guild not found with id " + guildID);
        }
        return guild;
    }

    public Collection<Guild> getAllGuilds() {
        return territoryRepository.findAll();
    }

    public Guild getGuildByLeader(UUID leaderId) {
        return territoryRepository.findGuildByLeader(leaderId);
    }

    public Guild getGuildByMember(UUID memberId) {
        return territoryRepository.findGuildByMember(memberId);
    }

    public GuildRole getMemberRole(UUID playerID) {
        return territoryRepository.findMemberRole(playerID);
    }

    public CreationResult createGuild(UUID playerID, Coordinate coordinate, String guildName) {
        if (playerService.isBaron()) {
            return CreationResult.RANK_TOO_LOW;
        }
        if (moneyService.getPlayerMoney(playerID) < GUILD_COST) {
            return CreationResult.NOT_ENOUGH_MONEY;
        }
        if (getByName(guildName) != null) {
            return CreationResult.NAME_TAKEN;
        }
        var result = isCreationAvailable(playerID, coordinate.chunkCoord());
        if (result != CreationResult.SUCCESS) return result;

        var guild = new Guild(playerID, guildName, coordinate);
        moneyService.payCreation(playerID,GUILD_COST);
        guild.claimInitialChunks();
        territoryRepository.save(guild);
        return result;
    }

    public Guild getByName(String guildName) {
        return territoryRepository.findGuildByName(guildName);
    }

    public SetSpawnResult setSpawnpoint(UUID requesterId, Coordinate coordinate, GuildSpawnKind kind) {
        if (kind == GuildSpawnKind.PRIVATE) {
            return setSpawnpoint(requesterId, coordinate);
        }
        return setVisitorSpawnpoint(requesterId, coordinate);
    }

    private SetSpawnResult setVisitorSpawnpoint(UUID player, Coordinate coordinate) {
        var guild = getByPlayer(player);
        if (guild == null) return SetSpawnResult.TERRITORY_NOT_FOUND;
        var result = guild.setVisitorSpawnpoint(coordinate, player);
        if (result == SetSpawnResult.SUCCESS) {
            territoryRepository.save(guild);
        }
        return result;
    }

    public Coordinate getPrivateSpawn(UUID guildId) {
        var guild = getById(guildId);
        if (guild == null) return null;
        return guild.getSpawnpoint();
    }

    public Coordinate getVisitorSpawn(UUID guildId) {
        var guild = getById(guildId);
        if (guild == null) return null;
        return guild.getVisitorSpawnpoint();
    }


    public boolean deleteGuildAsLeader(UUID leaderId) {
        var guild = territoryRepository.findGuildByLeader(leaderId);
        if (guild == null) return false;
        territoryRepository.deleteById(guild.getId());
        return true;
    }


    public boolean deleteGuildAsStaff(UUID guildId) {
        territoryRepository.deleteById(guildId);
        return true;
    }


    public GuildInvitationResult invite(UUID sender, UUID target) {
        var guild = getGuildByMember(sender);
        if (guild == null) return GuildInvitationResult.TERRITORY_NOT_FOUND;
        if (!guild.canInvite(sender)) return GuildInvitationResult.NOT_ALLOWED;
        if (sender.equals(target)) return GuildInvitationResult.SAME_PLAYER;
        if (getGuildByMember(target) != null) return GuildInvitationResult.HAS_GUILD;

        if (guild.getJoinRequests().contains(target)) {
            guild.join(target);
            territoryRepository.save(guild);
            return GuildInvitationResult.SUCCESS_JOIN;
        }
        guild.invitePlayer(target);
        eventService.publish(new GuildInvitationEvent(guild.getName(), sender, target));
        territoryRepository.save(guild);
        return GuildInvitationResult.SUCCESS_INVITE;
    }

    public boolean acceptInvitation(String guildName, UUID playerID) {
        var guild = getByName(guildName);
        if (guild == null) return false;

        boolean accepted;
        if (guild.getMembers().stream().anyMatch(m -> m.getPlayerID().equals(playerID))) {
            accepted = false;
        } else if (!guild.getInvitations().contains(playerID)) {
            accepted = false;
        } else {
            guild.join(playerID);
            territoryRepository.save(guild);
            accepted = true;
        }
        return accepted;
    }

    public boolean declineInvitation(String guildName, UUID playerID) {
        var guild = getByName(guildName);
        if (guild == null) return false;
        boolean removed = guild.removeInvitation(playerID);
        if (removed) {
            territoryRepository.save(guild);
        }
        return removed;
    }

    public void addMemberAsStaff(UUID guildID, UUID playerID) {
        var guild = getGuild(guildID);
        if (territoryRepository.findGuildByMember(playerID) != null) return;
        guild.join(playerID);
        territoryRepository.save(guild);
    }


    public StaffSetRoleResult setMemberRoleAsStaff(
            UUID guildID,
            UUID targetID,
            GuildRole newRole,
            UUID replacementLeaderID) {
        var guild = getGuild(guildID);
        var target = guild.getMembers().stream()
                .filter(m -> m.getPlayerID().equals(targetID))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + targetID));

        StaffSetRoleResult outcome;
        if (target.getRole() == newRole) {
            outcome = StaffSetRoleResult.SAME_ROLE;
        } else if (newRole == GuildRole.Leader) {
            var oldLeader = guild.getMembers().stream()
                    .filter(m -> m.getPlayerID().equals(guild.getLeaderId()))
                    .findFirst()
                    .orElseThrow();
            oldLeader.setRole(GuildRole.Citoyen);
            target.setRole(GuildRole.Leader);
            guild.setLeaderId(targetID);
            outcome = StaffSetRoleResult.SUCCESS;
        } else if (targetID.equals(guild.getLeaderId())) {
            if (replacementLeaderID == null) {
                outcome = StaffSetRoleResult.NEED_NEW_LEADER;
            } else if (replacementLeaderID.equals(targetID)) {
                outcome = StaffSetRoleResult.NEW_LEADER_SAME_AS_TARGET;
            } else {
                var newLeader = guild.getMembers().stream()
                        .filter(m -> m.getPlayerID().equals(replacementLeaderID))
                        .findFirst()
                        .orElseThrow();
                var oldLeader = guild.getMembers().stream()
                        .filter(m -> m.getPlayerID().equals(targetID))
                        .findFirst()
                        .orElseThrow();
                newLeader.setRole(GuildRole.Leader);
                oldLeader.setRole(newRole);
                guild.setLeaderId(replacementLeaderID);
                outcome = StaffSetRoleResult.SUCCESS;
            }
        } else {
            target.setRole(newRole);
            outcome = StaffSetRoleResult.SUCCESS;
        }

        if (outcome == StaffSetRoleResult.SUCCESS) {
            territoryRepository.save(guild);
        }
        return outcome;
    }


    public KickResult kickMember(UUID sender, UUID target) {
        var guild = getGuildByMember(sender);
        if (guild == null) return KickResult.TERRITORY_NOT_FOUND;
        if (!guild.canInvite(sender)) return KickResult.NOT_ALLOWED;
        if (guild.getMembers().stream().noneMatch(m -> m.getPlayerID().equals(target))) {
            return KickResult.NOT_MEMBER;
        }
        if (guild.getLeaderId().equals(target)) return KickResult.CANNOT_KICK_LEADER;
        guild.removeMember(target);
        territoryRepository.save(guild);
        return KickResult.SUCCESS;
    }


    public LeaveResult leaveGuild(UUID player) {
        var guild = getGuildByMember(player);
        if (guild == null) return LeaveResult.TERRITORY_NOT_FOUND;
        if (guild.getLeaderId().equals(player)) return LeaveResult.LEADER_MUST_DELETE;
        guild.removeMember(player);
        territoryRepository.save(guild);
        return LeaveResult.SUCCESS;
    }


    public boolean depositMoney(UUID guildID, UUID playerID, int amount) {
        var guild = getGuild(guildID);
        if (moneyService.getPlayerMoney(playerID) < amount) {
            return false;
        }
        require(guild.getMembers().stream().anyMatch(m -> m.getPlayerID().equals(playerID)),
                "Player " + playerID + " is not a member of guild " + guildID);
        moneyService.transfertToGuild(playerID,amount);
        guild.addMoney(amount);
        return true;
    }


    public boolean withdrawMoney(UUID guildID, UUID playerID, int amount) {
        var guild = getGuild(guildID);
        if(!guild.getMemberRole(playerID).canWithdrawMoney()){
            return false;
        }
        if (guild.getMoney() < amount) return false;
        moneyService.transfertFromGuild(playerID,amount);
        guild.addMoney(amount);
        return true;
    }

    public void giveMoneyAsStaff(UUID guildID, int amount) {
        var guild = getGuild(guildID);
        moneyService.transfertToGuildStaff(amount);
        territoryRepository.save(guild);
        guild.addMoney(amount);
    }

    private int xpToNextLevel(int level) {
        return XP_PER_LVL_MULTIPLICATOR * level;
    }


    public void addGuildXp(UUID guildId, int amount) {
        var guild = getGuild(guildId);
        guild.addXp(Math.max(amount, 0));
        territoryRepository.save(guild);
        upgradeGuildLevel(guildId);
    }


    public void addLevel(UUID guildId, int amount) {
        var guild = getGuild(guildId);
        guild.setLevel(guild.getLevel() + Math.max(amount, 0));
        guild.setXp(0);
        territoryRepository.save(guild);
    }

    public void setLevel(UUID guildId, int level) {
        var guild = getGuild(guildId);
        guild.setLevel(Math.max(level, 1));
        guild.setXp(0);
        territoryRepository.save(guild);
    }

    public boolean upgradeGuildLevel(UUID guildId) {
        var guild = getGuild(guildId);
        int need = xpToNextLevel(guild.getLevel());
        if (guild.getXp() < need) return false;

        guild.setXp(guild.getXp() - need);
        guild.setLevel(guild.getLevel() + 1);
        territoryRepository.save(guild);
        eventService.publish(new GuildLevelUpEvent(guild, guild.getLevel()));
        return true;
    }

    public GuildUpgradeResult upgradeRank(UUID guildId, GuildRank to) {
        var g = getGuild(guildId);
        if (to.ordinal() <= g.getRank().ordinal()) {
            return GuildUpgradeResult.ALREADY_AT_OR_ABOVE;
        }
        if (g.getLevel() < to.getMinLevel()) {
            return GuildUpgradeResult.RANK_LOCKED;
        }
        if (g.getMoney() < to.getCost()) {
            return GuildUpgradeResult.NOT_ENOUGH_MONEY;
        }
        g.setMoney(g.getMoney() - to.getCost());
        g.setRank(to);
        territoryRepository.save(g);
        return GuildUpgradeResult.SUCCESS;
    }

    public void setRank(UUID guildId, GuildRank rank) {
        var g = getGuild(guildId);
        g.setRank(rank);
        territoryRepository.save(g);
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}

