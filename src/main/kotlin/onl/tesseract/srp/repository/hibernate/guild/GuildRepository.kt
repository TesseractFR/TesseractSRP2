package onl.tesseract.srp.repository.hibernate.guild

import onl.tesseract.lib.repository.Repository
import onl.tesseract.srp.domain.campement.CampementChunk
import onl.tesseract.srp.domain.guild.Guild
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import java.util.*
import kotlin.jvm.optionals.getOrNull

interface GuildRepository : Repository<Guild, Int> {

    fun findGuildByChunk(chunk: CampementChunk): Guild?

    fun findGuildByName(name: String): Guild?

    fun findGuildByLeader(leaderID: UUID): Guild?
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
        return jpaRepository.findByChunksContains(GuildCityChunkEntity(chunk.x, chunk.z))
    }

    override fun idOf(entity: Guild) = entity.id

    override fun findGuildByName(name: String): Guild? {
        return jpaRepository.findByName(name)
    }

    override fun findGuildByLeader(leaderID: UUID): Guild? {
        return jpaRepository.findByLeaderId(leaderID)
    }
}

@org.springframework.stereotype.Repository
interface GuildJpaRepository : JpaRepository<GuildEntity, Int> {

    fun findByChunksContains(chunk: GuildCityChunkEntity): Guild?

    fun findByName(name: String): Guild?

    fun findByLeaderId(leaderID: UUID): Guild?
}