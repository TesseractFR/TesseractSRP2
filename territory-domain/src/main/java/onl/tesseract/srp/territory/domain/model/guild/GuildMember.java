package onl.tesseract.srp.territory.domain.model.guild;

import lombok.Getter;
import lombok.Setter;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRole;

import java.util.UUID;

@Getter
public class GuildMember {
    private final UUID playerID;
    @Setter
    private GuildRole role;

    public GuildMember(UUID playerID, GuildRole role) {
        this.playerID = playerID;
        this.role = role;
    }
}

