package onl.tesseract.srp.repository.hibernate.guild

import onl.tesseract.lib.repository.Repository
import onl.tesseract.srp.domain.guild.Guild
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrNull

interface GuildRepository : Repository<Guild, Int>

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

    override fun idOf(entity: Guild) = entity.id
}

@org.springframework.stereotype.Repository
interface GuildJpaRepository : JpaRepository<GuildEntity, Int>