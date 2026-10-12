package onl.tesseract.srp.territory.domain.model.guild;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.common.domain.model.Coordinate;
import onl.tesseract.srp.territory.domain.model.Territory;
import onl.tesseract.srp.territory.domain.model.container.DefaultVisitorSpawnContainer;
import onl.tesseract.srp.territory.domain.model.container.VisitorSpawnContainer;
import onl.tesseract.srp.territory.domain.model.enums.result.SetSpawnResult;
import onl.tesseract.srp.territory.domain.model.enums.result.UnclaimResult;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRank;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRole;
import onl.tesseract.srp.territory.domain.model.guild.event.GuildChunkClaimEvent;
import onl.tesseract.srp.territory.domain.model.guild.event.GuildChunkUnclaimEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
public class Guild extends Territory<GuildChunk> implements
        VisitorSpawnContainer,
        GuildMemberContainer {

    //Container
    private final GuildMemberContainer memberContainer;
    private final VisitorSpawnContainer visitorSpawnContainer;

    //Attributes
    private final String name;

    private int money;
    private int level;
    private int xp;
    private GuildRank rank;

    public Guild(UUID id, Coordinate spawnLocation, Set<UUID> trustedPlayers, GuildMemberContainer memberContainer,
                 VisitorSpawnContainer visitorSpawnContainer, String name, int money, int level, int xp,
                 GuildRank rank) {
        super(id, spawnLocation ,trustedPlayers);
        this.memberContainer = memberContainer;
        this.visitorSpawnContainer = visitorSpawnContainer;
        this.name = name;
        this.money = money;
        this.level = level;
        this.xp = xp;
        this.rank = rank;
        if (money < 0 || level < 1 || xp < 0 || rank == null) {
            throw new IllegalArgumentException("Invalid guild balance or progression state");
        }
    }

    public Guild(UUID leaderId, String name, Coordinate spawnLocation) {
        this(UUID.randomUUID(),spawnLocation,new HashSet<>(),new GuildMemberContainerImpl(leaderId),
                new DefaultVisitorSpawnContainer(spawnLocation),name,0,1,0,GuildRank.HAMEAU);
    }

    /**
     * @throws IllegalStateException If the guild already has chunks
     */
    @Override
    public void claimInitialChunks() {
        if (!_chunks.isEmpty()) {
            throw new IllegalStateException("Guild already has chunks");
        }

        ChunkCoord spawnChunk = getSpawnpoint().chunkCoord();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                _chunks.add(new GuildChunk(new ChunkCoord(spawnChunk.x() + x, spawnChunk.z() + z, spawnChunk.world()), this));
            }
        }
    }

    @Override
    public SetSpawnResult setVisitorSpawnpoint(Coordinate newLocation, UUID player) {
        if (!canSetSpawn(player)) return SetSpawnResult.NOT_ALLOWED;
        if (newLocation == null || !hasChunk(newLocation.chunkCoord())) return SetSpawnResult.OUTSIDE_TERRITORY;
        return visitorSpawnContainer.setVisitorSpawnpoint(newLocation, player);
    }

    @Override
    public Coordinate getVisitorSpawnpoint() {
        return visitorSpawnContainer.getVisitorSpawnpoint();
    }

    public void addMoney(int amount) {
        int updated = Math.addExact(money, amount);
        if (updated < 0) throw new IllegalArgumentException("Guild money cannot be negative");
        money = updated;
    }

    @Override
    public UnclaimResult unclaimChunk(ChunkCoord chunkCoord, UUID player) {
        Coordinate visitorSpawn = getVisitorSpawnpoint();
        if (visitorSpawn != null && visitorSpawn.chunkCoord().equals(chunkCoord)) {
            return UnclaimResult.IS_SPAWN_CHUNK;
        }
        return super.unclaimChunk(chunkCoord, player);
    }

    public void setMoney(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Guild money cannot be negative");
        this.money = amount;
    }

    public void addXp(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Guild XP cannot be negative");
        xp = Math.addExact(xp, amount);
    }

    /**
     * @throws IllegalArgumentException If the player is not a member of the guild
     */
    public GuildRole getMemberRole(UUID member) {
        List<GuildMember> members = memberContainer.getMembers();
        for (GuildMember m : members) {
            if (m.getPlayerID().equals(member)) {
                return m.getRole();
            }
        }
        throw new IllegalArgumentException("Player " + member + " is not a member of guild " + this.name);
    }

    @Override
    public GuildChunk initChunk(ChunkCoord chunkCoord) {
        return new GuildChunk(chunkCoord, this);
    }

    @Override
    public GuildChunkClaimEvent createClaimEvent(UUID player) {
        return new GuildChunkClaimEvent(player);
    }

    @Override
    public GuildChunkUnclaimEvent createUnclaimEvent(UUID player) {
        return new GuildChunkUnclaimEvent(player);
    }

    @Override
    public boolean canClaim(UUID player) {
        GuildRole role = getMemberRole(player);
        return role.canClaim();
    }

    @Override
    public boolean canBuild(UUID player) {
        return isTrusted(player) || hasMemberRole(player, GuildRole.Batisseur);
    }

    @Override
    public boolean canOpenChest(UUID player) {
        return isTrusted(player) || hasMember(player);
    }

    public boolean canInvite(UUID sender) {
        return hasMemberRole(sender, GuildRole.Adjoint);
    }

    public void setLevel(int level) {
        if (level < 1) throw new IllegalArgumentException("Guild level must be at least 1");
        this.level = level;
    }

    public void setXp(int xp) {
        if (xp < 0) throw new IllegalArgumentException("Guild XP cannot be negative");
        this.xp = xp;
    }

    public void setRank(GuildRank rank) {
        if (rank == null) throw new IllegalArgumentException("Guild rank cannot be null");
        this.rank = rank;
    }

    @Override
    public boolean canTrust(UUID player) {
        return hasMemberRole(player, GuildRole.Adjoint);
    }

    @Override
    public boolean canSetSpawn(UUID player) {
        return hasMemberRole(player, GuildRole.Adjoint);
    }

    private boolean hasMember(UUID player) {
        return memberContainer.getMembers().stream().anyMatch(member -> member.getPlayerID().equals(player));
    }

    private boolean hasMemberRole(UUID player, GuildRole minimumRole) {
        return memberContainer.getMembers().stream()
                .filter(member -> member.getPlayerID().equals(player))
                .anyMatch(member -> member.getRole().ordinal() >= minimumRole.ordinal());
    }


    // --- Delegated methods from GuildMemberContainer ---

    public UUID getLeaderId() {
        return memberContainer.getLeaderId();
    }

    public void setLeaderId(UUID leaderId) {
        memberContainer.setLeaderId(leaderId);
    }

    public List<GuildMember> getMembers() {
        return memberContainer.getMembers();
    }

    public java.util.Set<UUID> getInvitations() {
        return memberContainer.getInvitations();
    }

    public java.util.Set<UUID> getJoinRequests() {
        return memberContainer.getJoinRequests();
    }

    public void invitePlayer(UUID playerID) {
        memberContainer.invitePlayer(playerID);
    }

    public void askToJoin(UUID playerID) {
        memberContainer.askToJoin(playerID);
    }

    public void join(UUID playerID) {
        memberContainer.join(playerID);
    }

    public boolean removeInvitation(UUID playerID) {
        return memberContainer.removeInvitation(playerID);
    }

    public boolean removeMember(UUID playerID) {
        return memberContainer.removeMember(playerID);
    }

}
