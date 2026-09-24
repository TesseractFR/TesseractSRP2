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

    @Setter
    private int money;
    @Setter
    private int level;
    @Setter
    private int xp;
    @Setter
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
        return visitorSpawnContainer.setVisitorSpawnpoint(newLocation, player);
    }

    @Override
    public Coordinate getVisitorSpawnpoint() {
        return visitorSpawnContainer.getVisitorSpawnpoint();
    }

    public void addMoney(int amount) {
        money += amount;
    }

    public void addXp(int amount) {
        xp += amount;
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
    public boolean canSetSpawn(UUID player) {
        GuildRole role = getMemberRole(player);
        return role.canSetSpawn();
    }

    @Override
    public boolean canBuild(UUID player) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean canOpenChest(UUID player) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public boolean canInvite(UUID sender) {
        GuildRole role = getMemberRole(sender);
        return role.canInvite();
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
        memberContainer.removeMember(playerID);
        return false;
    }

}
