package onl.tesseract.srp.repository.hibernate.job

import onl.tesseract.lib.repository.Repository
import onl.tesseract.srp.domain.job.PlayerJobProgression
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import java.util.*

interface PlayerJobProgressionRepository : Repository<PlayerJobProgression, UUID>

@Component
class PlayerJobProgressionRepositoryJpaAdapter(private var jpaRepo: PlayerJobProgressionJpaRepository) : PlayerJobProgressionRepository {
    override fun getById(id: UUID): PlayerJobProgression? {
        return jpaRepo.findById(id)
            .orElse(null)
            ?.toDomain()
    }

    override fun save(entity: PlayerJobProgression): PlayerJobProgression {
        return jpaRepo.save(entity.toEntity()).toDomain()
    }

    override fun idOf(entity: PlayerJobProgression): UUID {
        return entity.playerID
    }
}

@org.springframework.stereotype.Repository
interface PlayerJobProgressionJpaRepository : JpaRepository<PlayerJobProgressionEntity, UUID>