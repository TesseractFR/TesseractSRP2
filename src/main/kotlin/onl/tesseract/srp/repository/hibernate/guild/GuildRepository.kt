package onl.tesseract.srp.repository.hibernate.guild

import onl.tesseract.lib.repository.Repository
import onl.tesseract.srp.domain.campement.CampementChunk
import onl.tesseract.srp.domain.guild.Guild
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component
import java.util.*
import kotlin.jvm.optionals.getOrNull

interface GuildRepository : Repository<Guild, Int> {

    fun findGuildByChunk(chunk: CampementChunk): Guild?

    fun findGuildByName(name: String): Guild?

    fun findGuildByLeader(leaderID: UUID): Guild?

    fun findGuildByMember(memberID: UUID): Guild?
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

    override fun findGuildByChunk(chunk: CampementChunk): Guild? {
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
}

@org.springframework.stereotype.Repository
interface GuildJpaRepository : JpaRepository<GuildEntity, Int> {

    fun findByChunksContains(chunk: GuildCityChunkEntity): GuildEntity?

    fun findByName(name: String): GuildEntity?

    fun findByLeaderId(leaderID: UUID): GuildEntity?

    @Query("FROM GuildEntity g JOIN GuildMemberEntity m on g.id = m.guildID WHERE m.playerID = :memberID")
    fun findByMember(@Param("memberID") memberID: UUID): GuildEntity?
}