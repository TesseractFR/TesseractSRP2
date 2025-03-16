package onl.tesseract.srp.repository.hibernate.campement

import onl.tesseract.lib.repository.Repository
import onl.tesseract.srp.domain.campement.Campement
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import java.util.*

interface CampementRepository : Repository<Campement, UUID> {
    fun getByOwnerID(ownerID: UUID): Campement?
    override fun save(entity: Campement)
    fun deleteById(id: UUID)
}

@Component
class CampementRepositoryJpaAdapter(private var jpaRepo: CampementJpaRepository) : CampementRepository {

    override fun getById(id: UUID): Campement? {
        return jpaRepo.findById(id)
            .orElse(null)
            ?.toDomain()
    }

    override fun save(entity: Campement) {
        jpaRepo.save(entity.toEntity())
    }

    override fun idOf(entity: Campement): UUID {
        return entity.id
    }

    override fun getByOwnerID(ownerID: UUID): Campement? {
        return jpaRepo.findByOwnerID(ownerID)?.toDomain()
    }

    override fun deleteById(id: UUID) {
        jpaRepo.deleteById(id)  // ✅ Vérifie que cette ligne est bien présente
    }
}

@org.springframework.stereotype.Repository
interface CampementJpaRepository : JpaRepository<CampementEntity, UUID> {
    fun findByOwnerID(ownerID: UUID): CampementEntity?
}
