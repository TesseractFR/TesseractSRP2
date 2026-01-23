package onl.tesseract.srp.repository.hibernate.territory

import onl.tesseract.srp.domain.territory.guild.Guild
import onl.tesseract.srp.domain.territory.guild.GuildChunk
import onl.tesseract.srp.domain.territory.guild.enum.GuildRole
import onl.tesseract.srp.repository.generic.territory.TerritoryChunkRepository
import onl.tesseract.srp.repository.generic.territory.GuildRepository
import onl.tesseract.srp.repository.hibernate.territory.entity.guild.GuildEntity
import onl.tesseract.srp.repository.hibernate.territory.entity.guild.toEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.util.*
import kotlin.jvm.optionals.getOrNull


@Component
class GuildRepositoryJpaAdapter(private val jpaRepository: GuildJpaRepository,
                                private val territoryChunkRepository: TerritoryChunkRepository) : GuildRepository {
    override fun getById(id: UUID): Guild? {
        return jpaRepository.findById(id)
            .map { it.toDomain() }
            .getOrNull()
    }

    override fun save(entity: Guild): Guild {
        return jpaRepository.save(entity.toEntity()).toDomain()
    }

    override fun findGuildByChunk(chunk: GuildChunk): Guild? {
        return territoryChunkRepository.findByIdAndType(chunk.chunkCoord, GuildChunk::class.java)?.guild
    }

    override fun idOf(entity: Guild) = entity.id

    override fun findGuildByName(name: String): Guild? {
        return jpaRepository.findByName((name))?.toDomain()
    }

    override fun findGuildByLeader(leaderID: UUID): Guild? {
        return jpaRepository.findByLeaderId(leaderID)?.toDomain()
    }

    override fun findGuildByMember(memberID: UUID): Guild? {
        return jpaRepository.findByMember(memberID)?.toDomain()
    }

    override fun findMemberRole(playerID: UUID): GuildRole? {
        return jpaRepository.findMemberRole(playerID)
    }

    override fun areChunksClaimed(chunks: Collection<GuildChunk>): Boolean {
        return chunks.any{ chunk ->
            territoryChunkRepository.getById(chunk.chunkCoord) != null
        }
    }

    override fun findAll(): Collection<Guild> {
        return jpaRepository.findAll().map { it.toDomain() }
    }

    override fun deleteById(id: UUID) {
        jpaRepository.deleteById(id)
    }

    override fun findnByPlayer(player: UUID): Guild? {
        return jpaRepository.findByMember(player)?.toDomain()
    }
}

@Repository
interface GuildJpaRepository : JpaRepository<GuildEntity, UUID> {

    fun findByName(name: String): GuildEntity?

    fun findByLeaderId(leaderID: UUID): GuildEntity?

    @Query("SELECT g FROM GuildEntity g JOIN g.members m WHERE m.playerID = :memberID")
    fun findByMember(@Param("memberID") memberID: UUID): GuildEntity?

    @Query("SELECT m.role FROM GuildMemberEntity m WHERE m.playerID = :playerID")
    fun findMemberRole(@Param("playerID") playerID: UUID): GuildRole?

}
