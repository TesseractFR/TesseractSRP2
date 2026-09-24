package onl.tesseract.srp.territory.domain.model.guild.event;
import java.util.*;

public record GuildInvitationEvent(
        String guild ,
        UUID sender,
    UUID target
){}
