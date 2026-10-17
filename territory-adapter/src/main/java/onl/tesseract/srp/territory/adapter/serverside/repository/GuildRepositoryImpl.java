package onl.tesseract.srp.territory.adapter.serverside.repository;

import onl.tesseract.srp.territory.adapter.serverside.entity.GuildEntity;
import onl.tesseract.srp.territory.domain.model.guild.Guild;
import onl.tesseract.srp.territory.domain.model.guild.GuildChunk;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRole;
import onl.tesseract.srp.territory.domain.port.serverside.GuildRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Repository
public class GuildRepositoryImpl implements GuildRepository {

    private final GuildJpaRepository jpaRepository;
    private final TerritoryChunkRepositoryImpl territoryChunkRepository;

    public GuildRepositoryImpl(GuildJpaRepository jpaRepository, TerritoryChunkRepositoryImpl territoryChunkRepository) {
        this.jpaRepository = jpaRepository;
        this.territoryChunkRepository = territoryChunkRepository;
    }

    @Override
    public Guild getById(UUID id) {
        return jpaRepository.findById(id).map(GuildEntity::toDomain).orElse(null);
    }

    @Override
    public Guild findByPlayer(UUID player) {
        return findGuildByMember(player);
    }

    @Override
    public Guild findGuildByChunk(GuildChunk chunk) {
        GuildEntity guild = jpaRepository.findGuildByChunk(chunk);
        return guild != null ? guild.toDomain() : null;
    }

    @Override
    public boolean areChunksClaimed(Collection<GuildChunk> chunks) {
        return chunks.stream().anyMatch(chunk -> territoryChunkRepository.getById(chunk.getChunkCoord()) == null);
    }

    @Override
    public Guild findGuildByName(String name) {
        GuildEntity guildByName = jpaRepository.findGuildByName(name);
        if(guildByName == null) return null;
        return guildByName.toDomain();
    }

    @Override
    public Guild findGuildByLeader(UUID leaderID) {
        GuildEntity guild = jpaRepository.findGuildByLeader(leaderID);
        return guild!=null?guild.toDomain():null;
    }

    @Override
    public Guild findGuildByMember(UUID memberID) {
        GuildEntity guild = jpaRepository.findGuildByMember(memberID);
        return guild != null ? guild.toDomain() : null;
    }

    @Override
    public GuildRole findMemberRole(UUID playerID) {
        return jpaRepository.findMemberRole(playerID);
    }

    @Override
    public Collection<Guild> findAll() {
        return jpaRepository.findAll().stream()
                .map(GuildEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Guild save(Guild guild) {
        jpaRepository.save(GuildEntity.fromDomain(guild));

        return guild;
    }

    @Override
    public UUID idOf(Guild guild) {
        return guild.getId();
    }
}

