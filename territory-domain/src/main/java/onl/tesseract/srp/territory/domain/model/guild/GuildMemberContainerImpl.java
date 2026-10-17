package onl.tesseract.srp.territory.domain.model.guild;

import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRole;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GuildMemberContainerImpl implements GuildMemberContainer {

    private UUID leaderId;
    private final List<GuildMember> members;
    private final Set<UUID> invitations;
    private final Set<GuildJoinRequest> joinRequests;

    public GuildMemberContainerImpl(UUID leaderId) {
        this(leaderId, List.of(), Set.of(), Set.of());
    }

    public GuildMemberContainerImpl(UUID leaderId, List<GuildMember> members, Set<UUID> invitations, Set<GuildJoinRequest> joinRequests) {
        this.leaderId = leaderId;
        this.members = new ArrayList<>(members);
        this.invitations = new HashSet<>(invitations);
        this.joinRequests = new HashSet<>(joinRequests);

        if (this.members.stream().map(GuildMember::getPlayerID).distinct().count() != this.members.size()) {
            throw new IllegalArgumentException("Guild member list contains duplicate players");
        }

        GuildMember leader = this.members.stream().filter(m -> m.getPlayerID().equals(leaderId)).findFirst().orElse(null);
        if (leader == null) {
            this.members.add(new GuildMember(leaderId, GuildRole.Leader));
        } else {
            leader.setRole(GuildRole.Leader);
        }
        this.members.stream()
                .filter(member -> !member.getPlayerID().equals(leaderId) && member.getRole() == GuildRole.Leader)
                .forEach(member -> member.setRole(GuildRole.Citoyen));
    }

    @Override
    public UUID getLeaderId() {
        return leaderId;
    }

    @Override
    public void setLeaderId(UUID leaderId) {
        requireNotNull(leaderId);
        GuildMember newLeader = members.stream()
                .filter(member -> member.getPlayerID().equals(leaderId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Leader must be a guild member"));
        members.stream()
                .filter(member -> member.getRole() == GuildRole.Leader && !member.getPlayerID().equals(leaderId))
                .forEach(member -> member.setRole(GuildRole.Citoyen));
        newLeader.setRole(GuildRole.Leader);
        this.leaderId = leaderId;
    }

    @Override
    public List<GuildMember> getMembers() {
        return Collections.unmodifiableList(members);
    }

    @Override
    public Set<UUID> getInvitations() {
        return Collections.unmodifiableSet(invitations);
    }

    @Override
    public Set<GuildJoinRequest> getJoinRequests() {
        return Collections.unmodifiableSet(joinRequests);
    }

    @Override
    public void invitePlayer(UUID playerID) {
        requireNotMember(playerID);
        invitations.add(playerID);
    }

    @Override
    public void askToJoin(UUID playerID, String message) {
        requireNotMember(playerID);
        joinRequests.add(new GuildJoinRequest(playerID, message, Instant.now()));
    }

    @Override
    public void join(UUID playerID) {
        requireNotMember(playerID);
        invitations.remove(playerID);
        removeJoinRequest(playerID);
        members.add(new GuildMember(playerID, GuildRole.Citoyen));
    }

    @Override
    public boolean removeInvitation(UUID playerID) {
        return invitations.remove(playerID);
    }

    @Override
    public boolean removeJoinRequest(UUID playerID) {
        return joinRequests.removeIf(r -> r.getPlayerID().equals(playerID));
    }

    @Override
    public boolean removeMember(UUID playerID) {
        if (playerID.equals(leaderId)) return false;
        invitations.remove(playerID);
        removeJoinRequest(playerID);
        return members.removeIf(m -> m.getPlayerID().equals(playerID));
    }

    private void requireNotMember(UUID playerID) {
        requireNotNull(playerID);
        boolean isMember = members.stream().anyMatch(m -> m.getPlayerID().equals(playerID));
        if (isMember) {
            throw new IllegalArgumentException("Player " + playerID + " is already a member");
        }
    }

    private void requireNotNull(UUID playerID) {
        if (playerID == null) throw new IllegalArgumentException("Player id cannot be null");
    }
}

