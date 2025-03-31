package onl.tesseract.srp.repository.hibernate.player

import onl.tesseract.srp.domain.player.SrpPlayer
import onl.tesseract.srp.repository.hibernate.SrpPlayerEntity
import onl.tesseract.srp.repository.hibernate.toEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.util.*

interface SrpPlayerRepository : onl.tesseract.lib.repository.Repository<SrpPlayer, UUID>

@Component
class SrpPlayerJpaRepositoryAdapter(private val jpaRepository: SrpPlayerJpaRepository) : SrpPlayerRepository {
    override fun getById(id: UUID): SrpPlayer? {
        return jpaRepository.findByIdOrNull(id)?.toDomain()
    }

    override fun save(entity: SrpPlayer): SrpPlayer {
        return jpaRepository.save(entity.toEntity()).toDomain()
    }

    override fun idOf(entity: SrpPlayer): UUID = entity.uniqueId
}

@Repository
interface SrpPlayerJpaRepository : JpaRepository<SrpPlayerEntity, UUID>