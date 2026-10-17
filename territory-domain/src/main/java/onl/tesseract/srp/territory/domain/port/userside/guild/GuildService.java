package onl.tesseract.srp.territory.domain.port.userside.guild;


import onl.tesseract.srp.common.domain.port.userside.plugin.EventPublisher;
import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.common.domain.model.Coordinate;
import onl.tesseract.srp.common.domain.model.enums.InteractionAllowResult;
import onl.tesseract.srp.territory.domain.model.TerritoryChunk;
import onl.tesseract.srp.territory.domain.model.enums.TerritoryWorld;
import onl.tesseract.srp.territory.domain.model.enums.result.CreationResult;
import onl.tesseract.srp.territory.domain.model.enums.result.SetSpawnResult;
import onl.tesseract.srp.territory.domain.model.guild.Guild;
import onl.tesseract.srp.territory.domain.model.guild.GuildChunk;
import onl.tesseract.srp.territory.domain.model.guild.enums.*;
import onl.tesseract.srp.territory.domain.model.guild.event.GuildInvitationEvent;
import onl.tesseract.srp.territory.domain.model.guild.event.GuildLevelUpEvent;
import onl.tesseract.srp.territory.domain.port.serverside.GuildRepository;
import onl.tesseract.srp.territory.domain.port.serverside.PlayerGuildMessagingRepository;
import onl.tesseract.srp.territory.domain.port.serverside.SrpPlayerRepository;
import onl.tesseract.srp.territory.domain.port.serverside.TerritoryChunkRepository;
import onl.tesseract.srp.territory.domain.port.userside.TerritoryService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class GuildService extends TerritoryService<GuildChunk, Guild> {
    private static final int GUILD_COST = 10_000;
    private static final int XP_PER_LVL_MULTIPLICATOR = 1000;

    private final GuildRepository territoryRepository;
    private final TerritoryChunkRepository territoryChunkRepository;
    private final SrpPlayerRepository playerService;
    private final EventPublisher eventService;
    private final MoneyService moneyService;
    private final PlayerGuildMessagingRepository messaging;

    public GuildService(
            GuildRepository territoryRepository,
            TerritoryChunkRepository territoryChunkRepository,
            SrpPlayerRepository playerService,
            EventPublisher eventService,
            MoneyService moneyService,
            PlayerGuildMessagingRepository messaging) {
        super();
        this.territoryRepository = territoryRepository;
        this.territoryChunkRepository = territoryChunkRepository;
        this.playerService = playerService;
        this.eventService = eventService;
        this.moneyService = moneyService;
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
        return territory.isTrusted(playerId)
                || (playerGuild != null && playerGuild.getId().equals(territory.getId()));
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

    public Guild getGuildByMember(UUID memberId) {
        return territoryRepository.findGuildByMember(memberId);
    }

    public GuildRole getMemberRole(UUID playerID) {
        return territoryRepository.findMemberRole(playerID);
    }

    public synchronized void createGuild(UUID playerID, Coordinate coordinate, String guildName) {
        if (coordinate == null) throw new IllegalArgumentException("Spawn coordinate cannot be null");
        if (guildName == null || guildName.isBlank()) throw new IllegalArgumentException("Guild name cannot be blank");
        if (!playerService.hasRequiredGuildRank(playerID)) {
            messaging.playerRankTooLowCreateGuild(playerID);
            return;
        }
        if (moneyService.getPlayerMoney(playerID) < GUILD_COST) {
            messaging.playerNoMoneyCreateGuild(playerID);
            return;
        }
        if (getByName(guildName) != null) {
            messaging.playerNameTakenCreateGuild(playerID);
            return;
        }
        var availability = isCreationAvailable(playerID, coordinate.chunkCoord());
        if (availability != CreationResult.SUCCESS) {
            switch (availability) {
                case INVALID_WORLD -> messaging.playerInvalidWorld(playerID);
                case NEAR_SPAWN -> messaging.playerNearSpawnCreateGuild(playerID);
                case TOO_CLOSE_TO_OTHER_TERRITORY -> messaging.playerTooCloseCreateGuild(playerID);
                case ALREADY_HAS_TERRITORY -> messaging.playerAlreadyHasGuildCreateGuild(playerID);
                case ON_OTHER_TERRITORY -> messaging.playerOnOtherTerritoryCreateGuild(playerID);
                default -> { /* Rien */ }
            }
            return;
        }
        var guild = new Guild(playerID, guildName, coordinate);
        guild.claimInitialChunks();
        if (!moneyService.payCreation(playerID, GUILD_COST)) {
            messaging.playerNoMoneyCreateGuild(playerID);
            return;
        }
        try {
            territoryRepository.save(guild);
        } catch (RuntimeException exception) {
            moneyService.transfertFromGuild(playerID, GUILD_COST);
            throw exception;
        }
        messaging.playerGuildCreated(playerID, guildName);
    }

    public Guild getByName(String guildName) {
        return territoryRepository.findGuildByName(guildName);
    }

    public SetSpawnResult setSpawnpoint(UUID requesterId, Coordinate coordinate, GuildSpawnKind kind) {
        if (kind == null) throw new IllegalArgumentException("Spawn kind cannot be null");
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

    public Coordinate resolveSpawnDestination(UUID player, Guild target) {
        boolean own = target == null;
        Guild guild = own ? getGuildByMember(player) : target;
        if (guild == null) {
            messaging.playerNoGuild(player);
            return null;
        }
        Coordinate destination = own
                ? guild.getSpawnpoint()
                : Optional.ofNullable(guild.getVisitorSpawnpoint()).orElse(guild.getSpawnpoint());
        if (destination == null) {
            messaging.playerNoSpawnDefined(player);
            return null;
        }
        return destination;
    }

    public void notifyTeleported(UUID player, Guild target) {
        messaging.playerTeleportedToSpawn(player,
                target != null ? target.getName() : null, target == null);
    }

    public boolean canDeleteGuild(UUID player) {
        var role = getMemberRole(player);
        if (role == null) {
            messaging.playerNoGuild(player);
            return false;
        }
        if (role != GuildRole.Leader) {
            messaging.playerNotAllowed(player);
            return false;
        }
        return true;
    }

    public synchronized void deleteGuild(UUID leader) {
        var guild = territoryRepository.findGuildByLeader(leader);
        if (guild == null) {
            messaging.playerDeleteGuildFailed(leader);
            return;
        }
        territoryRepository.deleteById(guild.getId());
        messaging.playerGuildDeleted(leader);
    }


    public synchronized void deleteGuildAsStaff(UUID staff, UUID guildId) {
        var guild = territoryRepository.getById(guildId);
        if (guild == null) {
            messaging.staffDeleteFailed(staff);
            return;
        }
        territoryRepository.deleteById(guildId);
        messaging.staffGuildDeleted(staff, guild.getName());
    }


    public synchronized void invitePlayer(UUID sender, UUID target) {
        var guild = getGuildByMember(sender);
        if (guild == null) {
            messaging.playerNoGuild(sender);
            return;
        }
        if (!guild.canInvite(sender)) {
            messaging.playerNotAllowed(sender);
            return;
        }
        if (sender.equals(target)) {
            messaging.playerCannotInviteSelf(sender);
            return;
        }
        if (getGuildByMember(target) != null) {
            messaging.playerInviteTargetHasGuild(sender, target);
            return;
        }

        if (guild.getJoinRequests().stream().anyMatch(r -> r.getPlayerID().equals(target))) {
            guild.join(target);
            territoryRepository.save(guild);
            messaging.playerInviteJoined(sender, target, guild.getName());
            return;
        }
        guild.invitePlayer(target);
        territoryRepository.save(guild);
        eventService.publish(new GuildInvitationEvent(guild.getName(), sender, target));
        messaging.playerInviteSent(sender, target);
    }

    public synchronized void requestJoin(UUID player, String guildName, String message) {
        var guild = getByName(guildName);
        if (guild == null) {
            messaging.playerJoinGuildNotFound(player, guildName);
            return;
        }
        if (getGuildByMember(player) != null) {
            messaging.playerAlreadyInGuild(player);
            return;
        }
        if (guild.getMembers().size() >= guild.getRank().getMaxMembersNumber()) {
            messaging.playerGuildFull(player);
            return;
        }
        if (guild.getJoinRequests().stream().anyMatch(r -> r.getPlayerID().equals(player))) {
            messaging.playerAlreadyRequested(player);
            return;
        }
        String text = (message == null || message.isBlank())
                ? messaging.defaultJoinRequestMessage()
                : message.trim();
        if (text.length() > 256) text = text.substring(0, 256);
        guild.askToJoin(player, text);
        territoryRepository.save(guild);
        messaging.playerJoinRequestSent(player, guild.getName());
    }

    public synchronized boolean acceptInvitation(String guildName, UUID playerID) {
        var guild = getByName(guildName);
        if (guild == null) return false;

        boolean accepted;
        if (guild.getMembers().stream().anyMatch(m -> m.getPlayerID().equals(playerID))) {
            accepted = false;
        } else if (territoryRepository.findGuildByMember(playerID) != null) {
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

    public synchronized boolean declineInvitation(String guildName, UUID playerID) {
        var guild = getByName(guildName);
        if (guild == null) return false;
        boolean removed = guild.removeInvitation(playerID);
        if (removed) territoryRepository.save(guild);
        return removed;
    }

    public synchronized void acceptJoinRequest(UUID sender, UUID target) {
        var guild = getGuildByMember(sender);
        if (guild == null) {
            messaging.playerNoGuild(sender);
            return;
        }
        if (!guild.canInvite(sender)) {
            messaging.playerNotAllowed(sender);
            return;
        }
        if (getGuildByMember(target) != null) {
            messaging.playerInviteTargetHasGuild(sender, target);
            return;
        }
        if (guild.getJoinRequests().stream().noneMatch(r -> r.getPlayerID().equals(target))) {
            messaging.playerNoJoinRequest(sender);
            return;
        }
        if (guild.getMembers().size() >= guild.getRank().getMaxMembersNumber()) {
            messaging.playerGuildFull(sender);
            return;
        }
        guild.join(target);
        territoryRepository.save(guild);
        messaging.playerJoinRequestAccepted(sender, target, guild.getName());
    }

    public synchronized void declineJoinRequest(UUID sender, UUID target) {
        var guild = getGuildByMember(sender);
        if (guild == null) {
            messaging.playerNoGuild(sender);
            return;
        }
        if (!guild.canInvite(sender)) {
            messaging.playerNotAllowed(sender);
            return;
        }
        if (!guild.removeJoinRequest(target)) {
            messaging.playerNoJoinRequest(sender);
            return;
        }
        territoryRepository.save(guild);
        messaging.playerJoinRequestDeclined(sender, target, guild.getName());
    }

    public synchronized boolean changeMemberRole(UUID sender, UUID target, GuildRoleChange change) {
        var guild = getGuildByMember(sender);
        if (guild == null) {
            messaging.playerNoGuild(sender);
            return false;
        }
        if (!guild.canInvite(sender)) {
            messaging.playerNotAllowed(sender);
            return false;
        }
        var member = guild.getMembers().stream()
                .filter(m -> m.getPlayerID().equals(target)).findFirst().orElse(null);
        if (member == null) {
            messaging.playerKickNotMember(sender, target);
            return false;
        }
        if (target.equals(guild.getLeaderId())) {
            messaging.playerRoleChangeImpossible(sender);
            return false;
        }
        boolean senderIsLeader = sender.equals(guild.getLeaderId());
        if (member.getRole() == GuildRole.Adjoint && !senderIsLeader) {
            messaging.playerNotAllowed(sender);
            return false;
        }

        if (change == GuildRoleChange.PROMOTE && member.getRole() == GuildRole.Adjoint) {
            guild.setLeaderId(target);
            guild.getMembers().stream().filter(m -> m.getPlayerID().equals(sender)).findFirst()
                    .ifPresent(m -> m.setRole(GuildRole.Adjoint));
            territoryRepository.save(guild);
            messaging.playerLeaderTransferred(sender, target);
            return true;
        }
        GuildRole newRole = switch (change) {
            case PROMOTE -> switch (member.getRole()) {
                case Citoyen -> GuildRole.Batisseur;
                case Batisseur -> GuildRole.Adjoint;
                default -> null;
            };
            case DEMOTE -> switch (member.getRole()) {
                case Adjoint -> GuildRole.Batisseur;
                case Batisseur -> GuildRole.Citoyen;
                default -> null;
            };
        };
        if (newRole == null) {
            messaging.playerRoleChangeImpossible(sender);
            return false;
        }
        member.setRole(newRole);
        territoryRepository.save(guild);
        messaging.playerRoleChanged(sender, target, newRole);
        return true;
    }


    public void showMembersAsStaff(UUID staff, UUID guildId) {
        var guild = getGuild(guildId);
        messaging.staffShowMembers(staff, guild.getName(), guild.getMembers());
    }

    public synchronized void addMemberAsStaff(UUID staff, UUID guildID, UUID playerID) {
        var guild = getGuild(guildID);
        if (guild.getMembers().stream().anyMatch(m -> m.getPlayerID().equals(playerID))) {
            messaging.staffMemberAlreadyInGuild(staff, playerID);
            return;
        }
        if (territoryRepository.findGuildByMember(playerID) != null) {
            messaging.staffMemberHasOtherGuild(staff, playerID);
            return;
        }
        guild.join(playerID);
        territoryRepository.save(guild);
        messaging.staffMemberAdded(staff, playerID, guild.getName());
    }


    public synchronized void kickMemberAsStaff(UUID staff, UUID guildID, UUID target) {
        var guild = getGuild(guildID);
        if (guild.getMembers().stream().noneMatch(m -> m.getPlayerID().equals(target))) {
            messaging.staffMemberNotInGuild(staff, target);
            return;
        }
        if (guild.getLeaderId().equals(target)) {
            messaging.staffCannotKickLeader(staff);
            return;
        }
        guild.removeMember(target);
        territoryRepository.save(guild);
        messaging.staffMemberKicked(staff, target, guild.getName());
    }

    public synchronized void setMemberRoleAsStaff(UUID staff, UUID guildID, UUID targetID,
                                                  GuildRole newRole, UUID replacementLeaderID) {
        var guild = getGuild(guildID);
        if (targetID == null) {
            messaging.staffPlayerNotFound(staff);
            return;
        }
        var target = guild.getMembers().stream()
                .filter(m -> m.getPlayerID().equals(targetID))
                .findFirst()
                .orElse(null);
        if (target == null) {
            messaging.staffMemberNotInGuild(staff, targetID);
            return;
        }
        if (target.getRole() == newRole) {
            messaging.staffSameRole(staff, targetID, newRole, guild.getName());
            return;
        }
        if (newRole == GuildRole.Leader) {
            UUID oldLeaderId = guild.getLeaderId();
            var oldLeader = guild.getMembers().stream()
                    .filter(m -> m.getPlayerID().equals(oldLeaderId)).findFirst().orElseThrow();
            oldLeader.setRole(GuildRole.Citoyen);
            target.setRole(GuildRole.Leader);
            guild.setLeaderId(targetID);
            territoryRepository.save(guild);
            messaging.staffLeaderSet(staff, guild.getName(), targetID, oldLeaderId);
        } else if (targetID.equals(guild.getLeaderId())) {
            if (replacementLeaderID == null) {
                messaging.staffNeedNewLeader(staff, guild.getName(), targetID, newRole);
            } else if (replacementLeaderID.equals(targetID)) {
                messaging.staffNewLeaderSameAsTarget(staff);
            } else {
                var newLeader = guild.getMembers().stream()
                        .filter(m -> m.getPlayerID().equals(replacementLeaderID)).findFirst().orElseThrow();
                newLeader.setRole(GuildRole.Leader);
                target.setRole(newRole);
                guild.setLeaderId(replacementLeaderID);
                territoryRepository.save(guild);
                messaging.staffRoleSetWithReplacement(staff, targetID, newRole, replacementLeaderID, guild.getName());
            }
        } else {
            target.setRole(newRole);
            territoryRepository.save(guild);
            messaging.staffRoleSet(staff, targetID, newRole, guild.getName());
        }
    }


    public boolean canKick(UUID sender, UUID target) {
        var guild = getGuildByMember(sender);
        if (guild == null) {
            messaging.playerNoGuild(sender);
            return false;
        }
        if (!guild.canInvite(sender)) {
            messaging.playerNotAllowed(sender);
            return false;
        }
        if (guild.getMembers().stream().noneMatch(m -> m.getPlayerID().equals(target))) {
            messaging.playerKickNotMember(sender, target);
            return false;
        }
        if (guild.getLeaderId().equals(target)) {
            messaging.playerCannotKickLeader(sender);
            return false;
        }
        return true;
    }

    public synchronized void kick(UUID sender, UUID target) {
        if (!canKick(sender, target)) return;
        var guild = getGuildByMember(sender);
        guild.removeMember(target);
        territoryRepository.save(guild);
        messaging.playerKicked(sender, target);
    }


    public boolean canLeave(UUID player) {
        var guild = getGuildByMember(player);
        if (guild == null) {
            messaging.playerNoGuild(player);
            return false;
        }
        if (guild.getLeaderId().equals(player)) {
            messaging.playerLeaderMustDelete(player);
            return false;
        }
        return true;
    }

    public synchronized void leave(UUID player) {
        if (!canLeave(player)) return;
        var guild = getGuildByMember(player);
        guild.removeMember(player);
        territoryRepository.save(guild);
        messaging.playerLeftGuild(player);
    }


    public void claim(UUID player, ChunkCoord chunk) {
        switch (claimChunk(player, chunk)) {
            case SUCCESS -> messaging.playerChunkClaimed(player, chunk.x(), chunk.z());
            case TERRITORY_NOT_FOUND -> messaging.playerNoGuild(player);
            case ALREADY_OWNED -> messaging.playerChunkAlreadyOwned(player);
            case ALREADY_OTHER -> messaging.playerChunkOwnedByOther(player);
            case NOT_ADJACENT -> messaging.playerChunkNotAdjacent(player);
            case NOT_ALLOWED -> messaging.playerNotAllowed(player);
            case TOO_CLOSE -> messaging.playerChunkTooClose(player);
            case INVALID_WORLD -> messaging.playerInvalidWorld(player);
        }
    }

    public void unclaim(UUID player, ChunkCoord chunk) {
        switch (unclaimChunk(player, chunk)) {
            case SUCCESS -> messaging.playerChunkUnclaimed(player, chunk.x(), chunk.z());
            case NOT_OWNED -> messaging.playerChunkNotOwned(player);
            case LAST_CHUNK -> messaging.playerLastChunk(player);
            case IS_SPAWN_CHUNK -> messaging.playerChunkIsSpawn(player);
            case SPLIT -> messaging.playerChunkSplit(player);
            case NOT_ALLOWED -> messaging.playerNotAllowed(player);
            case TERRITORY_NOT_FOUND -> messaging.playerNoGuild(player);
        }
    }

    public void defineSpawn(UUID player, Coordinate coordinate, GuildSpawnKind kind) {
        switch (setSpawnpoint(player, coordinate, kind)) {
            case SUCCESS -> messaging.playerSpawnSet(player, kind == GuildSpawnKind.VISITOR);
            case NOT_ALLOWED -> messaging.playerNotAllowed(player);
            case OUTSIDE_TERRITORY -> messaging.playerSpawnOutsideTerritory(player);
            case TERRITORY_NOT_FOUND -> messaging.playerNoGuild(player);
        }
    }


    public synchronized boolean depositMoney(UUID guildID, UUID playerID, int amount) {
        if (amount <= 0) return false;
        var guild = getGuild(guildID);
        require(guild.getMembers().stream().anyMatch(m -> m.getPlayerID().equals(playerID)),
                "Player " + playerID + " is not a member of guild " + guildID);
        if (moneyService.getPlayerMoney(playerID) < amount || !moneyService.transfertToGuild(playerID, amount)) {
            return false;
        }
        guild.addMoney(amount);
        try {
            territoryRepository.save(guild);
            return true;
        } catch (RuntimeException exception) {
            guild.addMoney(-amount);
            moneyService.transfertFromGuild(playerID, amount);
            throw exception;
        }
    }


    public synchronized boolean withdrawMoney(UUID guildID, UUID playerID, int amount) {
        if (amount <= 0) return false;
        var guild = getGuild(guildID);
        if (guild.getMembers().stream().noneMatch(member -> member.getPlayerID().equals(playerID))) return false;
        if(!guild.getMemberRole(playerID).canWithdrawMoney()){
            return false;
        }
        if (guild.getMoney() < amount) return false;
        if (!moneyService.transfertFromGuild(playerID, amount)) return false;
        guild.addMoney(-amount);
        try {
            territoryRepository.save(guild);
            return true;
        } catch (RuntimeException exception) {
            guild.addMoney(amount);
            moneyService.transfertToGuild(playerID, amount);
            throw exception;
        }
    }

    public synchronized boolean bankDeposit(UUID player, int amount) {
        var guild = getGuildByMember(player);
        if (guild == null) {
            messaging.playerNoGuild(player);
            return false;
        }
        if (amount <= 0) {
            messaging.playerInvalidAmount(player);
            return false;
        }
        if (!depositMoney(guild.getId(), player, amount)) {
            messaging.playerNotEnoughMoney(player);
            return false;
        }
        messaging.playerBankDeposited(player, amount);
        return true;
    }

    public synchronized boolean bankWithdraw(UUID player, int amount) {
        var guild = getGuildByMember(player);
        if (guild == null) {
            messaging.playerNoGuild(player);
            return false;
        }
        if (amount <= 0) {
            messaging.playerInvalidAmount(player);
            return false;
        }
        if (!guild.getMemberRole(player).canWithdrawMoney()) {
            messaging.playerNotAllowed(player);
            return false;
        }
        if (!withdrawMoney(guild.getId(), player, amount)) {
            messaging.playerGuildNotEnoughMoney(player);
            return false;
        }
        messaging.playerBankWithdrawn(player, amount);
        return true;
    }

    public void invalidAmount(UUID player) {
        messaging.playerInvalidAmount(player);
    }

    public void giveMoneyAsStaff(UUID staff, UUID guildID, int amount) {
        if (amount <= 0) {
            messaging.staffInvalidAmount(staff);
            return;
        }
        var guild = getGuild(guildID);
        guild.addMoney(amount);
        territoryRepository.save(guild);
        messaging.staffMoneyGiven(staff, guild.getName(), amount);
    }

    public void takeMoneyAsStaff(UUID staff, UUID guildID, int amount) {
        if (amount <= 0) {
            messaging.staffInvalidAmount(staff);
            return;
        }
        var guild = getGuild(guildID);
        if (guild.getMoney() < amount) {
            messaging.staffNotEnoughGuildMoney(staff, guild.getName());
            return;
        }
        guild.addMoney(-amount);
        territoryRepository.save(guild);
        messaging.staffMoneyTaken(staff, guild.getName(), amount);
    }

    public void showMoneyAsStaff(UUID staff, UUID guildId) {
        var guild = getGuild(guildId);
        messaging.staffShowMoney(staff, guild.getName(), guild.getMoney());
    }

    private long xpToNextLevel(int level) {
        return (long) XP_PER_LVL_MULTIPLICATOR * level;
    }

    public void addGuildXpAsStaff(UUID staff, UUID guildId, int amount) {
        if (amount <= 0) {
            messaging.staffInvalidAmount(staff);
            return;
        }
        var guild = getGuild(guildId);
        guild.addXp(amount);
        List<Integer> levelUps = new ArrayList<>();
        while (guild.getXp() >= xpToNextLevel(guild.getLevel())) {
            long need = xpToNextLevel(guild.getLevel());
            guild.setXp(Math.toIntExact(guild.getXp() - need));
            guild.setLevel(Math.addExact(guild.getLevel(), 1));
            levelUps.add(guild.getLevel());
        }
        territoryRepository.save(guild);
        levelUps.forEach(level -> eventService.publish(new GuildLevelUpEvent(guild, level)));
        messaging.staffXpAdded(staff, guild.getName(), amount);
    }

    public void addLevelAsStaff(UUID staff, UUID guildId, int amount) {
        if (amount <= 0) {
            messaging.staffInvalidAmount(staff);
            return;
        }
        var guild = getGuild(guildId);
        guild.setLevel(Math.addExact(guild.getLevel(), amount));
        guild.setXp(0);
        territoryRepository.save(guild);
        messaging.staffLevelAdded(staff, guild.getName(), amount);
    }

    public void setLevelAsStaff(UUID staff, UUID guildId, int level) {
        var guild = getGuild(guildId);
        guild.setLevel(Math.max(level, 1));
        guild.setXp(0);
        territoryRepository.save(guild);
        messaging.staffLevelSet(staff, guild.getName(), guild.getLevel());
    }

    public void showLevelAsStaff(UUID staff, UUID guildId) {
        var guild = getGuild(guildId);
        messaging.staffShowLevel(staff, guild.getName(), guild.getLevel());
    }

    public void setRankAsStaff(UUID staff, UUID guildId, GuildRank rank) {
        if (rank == null) throw new IllegalArgumentException("Rank cannot be null");
        var guild = getGuild(guildId);
        guild.setRank(rank);
        territoryRepository.save(guild);
        messaging.staffRankSet(staff, guild.getName(), rank);
    }

    public boolean upgradeGuildLevel(UUID guildId) {
        var guild = getGuild(guildId);
        long need = xpToNextLevel(guild.getLevel());
        if (guild.getXp() < need) return false;

        guild.setXp(Math.toIntExact(guild.getXp() - need));
        guild.setLevel(Math.addExact(guild.getLevel(), 1));
        territoryRepository.save(guild);
        int newLevel = guild.getLevel();
        eventService.publish(new GuildLevelUpEvent(guild, newLevel));
        return true;
    }

    public void showRankAsStaff(UUID staff, UUID guildId) {
        var guild = getGuild(guildId);
        messaging.staffShowRank(staff, guild.getName(), guild.getRank());
    }

    public synchronized boolean upgradeRank(UUID player) {
        var guild = getGuildByMember(player);
        if (guild == null) {
            messaging.playerNoGuild(player);
            return false;
        }
        if (!guild.getMemberRole(player).canWithdrawMoney()) {
            messaging.playerNotAllowed(player);
            return false;
        }
        GuildRank next = guild.getRank().next();
        if (next == null) {
            messaging.playerRankMax(player);
            return false;
        }
        if (guild.getLevel() < next.getMinLevel()) {
            messaging.playerRankLevelMissing(player, next.getMinLevel());
            return false;
        }
        if (guild.getMoney() < next.getCost()) {
            messaging.playerRankMoneyMissing(player, next.getCost());
            return false;
        }
        guild.addMoney(-next.getCost());
        guild.setRank(next);
        territoryRepository.save(guild);
        messaging.playerRankUpgraded(player, next);
        return true;
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    public boolean canOpenMenu(UUID player) {
        if (getGuildByMember(player) == null) {
            messaging.playerNoGuild(player);
            return false;
        }
        return true;
    }
}