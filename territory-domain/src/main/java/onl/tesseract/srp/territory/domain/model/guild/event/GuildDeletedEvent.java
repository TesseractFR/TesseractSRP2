package onl.tesseract.srp.territory.domain.model.guild.event;

import java.util.UUID;

/** Publié quand une guilde est supprimée, pour que les modules qui lui rattachent des données les suppriment aussi. */
public record GuildDeletedEvent(UUID guildId) {
}
