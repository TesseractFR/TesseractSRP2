package onl.tesseract.srp.territory.domain.model.guild;

import lombok.Getter;
import lombok.Setter;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRole;

import java.time.Instant;
import java.util.UUID;

@Getter
public class GuildMember {
    private final UUID playerID;
    @Setter
    private GuildRole role;
    private final Instant joinedDate;

    public GuildMember(UUID playerID, GuildRole role) {
        this(playerID, role, Instant.now());
    }

    public GuildMember(UUID playerID, GuildRole role, Instant joinedDate) {
        this.playerID = playerID;
        this.role = role;
        this.joinedDate = joinedDate;
    }
}