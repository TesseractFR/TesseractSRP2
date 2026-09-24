package onl.tesseract.srp.territory.domain.model.guild;

import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRole;

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
    private final Set<UUID> joinRequests;

    public GuildMemberContainerImpl(UUID leaderId) {
        this(leaderId, List.of(), Set.of(), Set.of());
    }

    public GuildMemberContainerImpl(UUID leaderId, List<GuildMember> members, Set<UUID> invitations, Set<UUID> joinRequests) {
        this.leaderId = leaderId;
        this.members = new ArrayList<>(members);
        this.invitations = new HashSet<>(invitations);
        this.joinRequests = new HashSet<>(joinRequests);

        boolean hasLeader = this.members.stream().anyMatch(m -> m.getPlayerID().equals(leaderId));
        if (!hasLeader) {
            this.members.add(new GuildMember(leaderId, GuildRole.Leader));
        }
    }

    @Override
    public UUID getLeaderId() {
        return leaderId;
    }

    @Override
    public void setLeaderId(UUID leaderId) {
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
    public Set<UUID> getJoinRequests() {
        return Collections.unmodifiableSet(joinRequests);
    }

    @Override
    public void invitePlayer(UUID playerID) {
        requireNotMember(playerID);
        invitations.add(playerID);
    }

    @Override
    public void askToJoin(UUID playerID) {
        requireNotMember(playerID);
        joinRequests.add(playerID);
    }

    @Override
    public void join(UUID playerID) {
        requireNotMember(playerID);
        invitations.remove(playerID);
        joinRequests.remove(playerID);
        members.add(new GuildMember(playerID, GuildRole.Citoyen));
    }

    @Override
    public boolean removeInvitation(UUID playerID) {
        return invitations.remove(playerID);
    }

    @Override
    public boolean removeMember(UUID playerID) {
        if (playerID.equals(leaderId)) return false;
        invitations.remove(playerID);
        joinRequests.remove(playerID);
        return members.removeIf(m -> m.getPlayerID().equals(playerID));
    }

    private void requireNotMember(UUID playerID) {
        boolean isMember = members.stream().anyMatch(m -> m.getPlayerID().equals(playerID));
        if (isMember) {
            throw new IllegalArgumentException("Player " + playerID + " is already a member");
        }
    }
}

