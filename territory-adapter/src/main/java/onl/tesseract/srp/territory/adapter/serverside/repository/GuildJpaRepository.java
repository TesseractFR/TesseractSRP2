package onl.tesseract.srp.territory.adapter.serverside.repository;

import onl.tesseract.srp.territory.adapter.serverside.entity.GuildEntity;
import onl.tesseract.srp.territory.domain.model.guild.Guild;
import onl.tesseract.srp.territory.domain.model.guild.GuildChunk;
import onl.tesseract.srp.territory.domain.model.guild.enums.GuildRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface GuildJpaRepository extends JpaRepository<GuildEntity, UUID> {

    @Query("SELECT g FROM GuildEntity g WHERE g.id IN (SELECT gm.guildId FROM GuildChunkEntity gm WHERE gm.chunk = :chunk)")
    GuildEntity findGuildByChunk(@Param("chunk") GuildChunk chunk);

    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM GuildEntity g WHERE g.id IN :ids")
    boolean areChunksClaimed(@Param("ids") Collection<UUID> ids);

    @Query("SELECT g FROM GuildEntity g WHERE g.name = :name")
    GuildEntity findGuildByName(@Param("name") String name);

    @Query("SELECT g FROM GuildEntity g WHERE g.id = :leaderId")
    GuildEntity findGuildByLeader(@Param("leaderId") UUID leaderId);

    @Query("SELECT g FROM GuildEntity g JOIN g.members m WHERE m.playerID = :memberId")
    GuildEntity findGuildByMember(@Param("memberId") UUID memberId);

    @Query("SELECT m.role FROM GuildEntity g JOIN g.members m WHERE m.playerID = :playerId")
    GuildRole findMemberRole(@Param("playerId") UUID playerId);

    @Override
    List<GuildEntity> findAll();

    @Override
    void deleteById(UUID id);

    @Override
    @Query("SELECT g FROM GuildEntity g WHERE g.id = :id")
    GuildEntity getById(UUID id);

    @Query("SELECT g FROM GuildEntity g WHERE g.id = :id")
    GuildEntity findByPlayer(UUID player);
}

