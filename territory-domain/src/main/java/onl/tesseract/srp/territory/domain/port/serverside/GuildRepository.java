package onl.tesseract.srp.territory.domain.port.serverside;

import onl.tesseract.srp.territory.domain.model.guild.Guild;
import onl.tesseract.srp.territory.domain.model.guild.GuildChunk;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRole;

import java.util.Collection;
import java.util.UUID;

public interface GuildRepository extends TerritoryRepository<Guild, UUID> {
    Guild findGuildByChunk(GuildChunk chunk);

    boolean areChunksClaimed(Collection<GuildChunk> chunks);

    Guild findGuildByName(String name);

    Guild findGuildByLeader(UUID leaderID);

    Guild findGuildByMember(UUID memberID);

    GuildRole findMemberRole(UUID playerID);

    Collection<Guild> findAll();

    void deleteById(UUID id);
}

