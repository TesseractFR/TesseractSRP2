package onl.tesseract.srp.territory.domain.model.guild;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface GuildMemberContainer {
    UUID getLeaderId();

    void setLeaderId(UUID leaderId);

    List<GuildMember> getMembers();

    Set<UUID> getInvitations();

    Set<GuildJoinRequest> getJoinRequests();

    void invitePlayer(UUID playerID);
    void askToJoin(UUID playerID, String message);
    void join(UUID playerID);
    boolean removeInvitation(UUID playerID);
    boolean removeJoinRequest(UUID playerID);
    boolean removeMember(UUID playerID);
}
