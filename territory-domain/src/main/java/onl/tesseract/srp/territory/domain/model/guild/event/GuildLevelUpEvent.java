package onl.tesseract.srp.territory.domain.model.guild.event;

import onl.tesseract.srp.territory.domain.model.guild.Guild;

public record GuildLevelUpEvent(
    Guild guild,
    int level
) {}