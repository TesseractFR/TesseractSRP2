package onl.tesseract.srp.repository.hibernate

import onl.tesseract.lib.repository.Repository
import onl.tesseract.srp.domain.campement.Campement
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component
import java.util.*

interface CampementRepository : Repository<Campement, UUID> {
    fun deleteById(id: UUID)
    fun isChunkClaimed(chunk: String): Boolean
    fun getCampementByChunk(chunk: String): Campement?
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

    override fun deleteById(id: UUID) {
        jpaRepo.deleteById(id)
    }

    override fun isChunkClaimed(chunk: String): Boolean {
        return jpaRepo.findCampementByChunk(chunk) != null
    }

    override fun getCampementByChunk(chunk: String): Campement? {
        return jpaRepo.findCampementByChunk(chunk)?.toDomain()
    }
}

@org.springframework.stereotype.Repository
interface CampementJpaRepository : JpaRepository<CampementEntity, UUID> {
    @Query("SELECT ce FROM CampementEntity ce JOIN ce.listChunks c WHERE c = :chunk")
    fun findCampementByChunk(@Param("chunk") chunk: String): CampementEntity?
}

