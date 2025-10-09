package onl.tesseract.srp.repository.hibernate.guild

import onl.tesseract.lib.repository.Repository
import onl.tesseract.srp.domain.guild.Guild
import onl.tesseract.srp.domain.guild.GuildChunk
import onl.tesseract.srp.domain.guild.GuildRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component
import java.util.*
import kotlin.jvm.optionals.getOrNull

interface GuildRepository : Repository<Guild, Int> {
    fun findGuildByChunk(chunk: GuildChunk): Guild?
    fun areChunksClaimed(chunks: Collection<GuildChunk>): Boolean
    fun findGuildByName(name: String): Guild?
    fun findGuildByLeader(leaderID: UUID): Guild?
    fun findGuildByMember(memberID: UUID): Guild?
    fun findMemberRole(playerID: UUID): GuildRole?
    fun findAll(): Collection<Guild>
    fun deleteById(id: Int)
}

@Component
class GuildRepositoryJpaAdapter(private val jpaRepository: GuildJpaRepository) : GuildRepository {
    override fun getById(id: Int): Guild? {
        return jpaRepository.findById(id)
            .map { it.toDomain() }
            .getOrNull()
    }

    override fun save(entity: Guild): Guild {
        return jpaRepository.save(entity.toEntity()).toDomain()
    }

    override fun findGuildByChunk(chunk: GuildChunk): Guild? {
        return jpaRepository.findByChunksContains(GuildCityChunkEntity(chunk.x, chunk.z))?.toDomain()
    }

    override fun idOf(entity: Guild) = entity.id

    override fun findGuildByName(name: String): Guild? {
        return jpaRepository.findByName(name)?.toDomain()
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
        return jpaRepository.getAnyChunkClaimed(chunks.map { "${it.x},${it.z}" })
    }

    override fun findAll(): Collection<Guild> {
        return jpaRepository.findAll().map { it.toDomain() }
    }

    override fun deleteById(id: Int) {
        jpaRepository.deleteById(id)
    }
}

@org.springframework.stereotype.Repository
interface GuildJpaRepository : JpaRepository<GuildEntity, Int> {

    fun findByChunksContains(chunk: GuildCityChunkEntity): GuildEntity?

    @Query("""SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END 
        FROM GuildEntity g JOIN g.chunks c
        WHERE c.coordinates IN :chunks """)
    fun getAnyChunkClaimed(@Param("chunks") chunks: Collection<String>): Boolean

    fun findByName(name: String): GuildEntity?

    fun findByLeaderId(leaderID: UUID): GuildEntity?

    @Query("SELECT g FROM GuildEntity g JOIN g.members m WHERE m.playerID = :memberID")
    fun findByMember(@Param("memberID") memberID: UUID): GuildEntity?

    @Query("SELECT m.role FROM GuildMemberEntity m WHERE m.playerID = :playerID")
    fun findMemberRole(@Param("playerID") playerID: UUID): GuildRole?

}
