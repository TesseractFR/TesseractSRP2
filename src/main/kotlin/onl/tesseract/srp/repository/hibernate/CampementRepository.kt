package onl.tesseract.srp.repository.hibernate

import jakarta.persistence.QueryHint
import onl.tesseract.lib.repository.Repository
import onl.tesseract.srp.domain.campement.Campement
import org.hibernate.jpa.HibernateHints
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.stereotype.Component
import java.util.*

interface CampementRepository : Repository<Campement, UUID> {
    fun deleteById(id: UUID)
    fun isChunkClaimed(x: Int, z: Int): Boolean
    fun getCampementByChunk(x: Int, z: Int): Campement?
    fun findAll(): List<Campement>
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
        return entity.ownerID
    }

    override fun deleteById(id: UUID) {
        jpaRepo.deleteById(id)
    }

    override fun isChunkClaimed(x: Int, z: Int): Boolean {
        return jpaRepo.isChunkClaimed(x, z)
    }

    override fun getCampementByChunk(x: Int, z: Int): Campement? {
        return jpaRepo.findByListChunksContains(CampementChunkEntity(x, z))?.toDomain()
    }

    override fun findAll(): List<Campement> {
        return jpaRepo.findAll().map { it.toDomain() }
    }

}

@org.springframework.stereotype.Repository
interface CampementJpaRepository : JpaRepository<CampementEntity, UUID> {
    @QueryHints(QueryHint(name = HibernateHints.HINT_CACHEABLE, value = "true"))
    fun findByListChunksContains(chunk: CampementChunkEntity): CampementEntity?

    @Query("SELECT exists(FROM CampementChunkEntity c WHERE c.x = :x AND c.z = :z)")
    fun isChunkClaimed(x: Int, z: Int): Boolean
}

