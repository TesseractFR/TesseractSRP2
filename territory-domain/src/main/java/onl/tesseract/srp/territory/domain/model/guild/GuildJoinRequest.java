package onl.tesseract.srp.territory.domain.model.guild;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class GuildJoinRequest {
    private final UUID playerID;
    private final String message;
    private final Instant requestedDate;
}
