package onl.tesseract.srp.repository.hibernate

import onl.tesseract.srp.domain.territory.campement.Campement
import onl.tesseract.srp.domain.territory.campement.CampementChunk
import onl.tesseract.srp.domain.commun.ChunkCoord
import onl.tesseract.srp.repository.generic.territory.TerritoryChunkRepository
import onl.tesseract.srp.repository.generic.territory.TerritoryRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import java.util.*

interface CampementRepository : TerritoryRepository<Campement, UUID> {
    fun deleteById(id: UUID)
    fun isChunkClaimed(chunkCoord: ChunkCoord): Boolean
    fun getCampementByChunk(chunkCoord: ChunkCoord): Campement?
    fun findAll(): List<Campement>
}

@Component
class CampementRepositoryJpaAdapter(private var jpaRepo: CampementJpaRepository,
    private val territoryChunkRepository: TerritoryChunkRepository) : CampementRepository {

    override fun getById(id: UUID): Campement? {
        return jpaRepo.findById(id)
            .orElse(null)
            ?.toDomain()
    }

    override fun save(entity: Campement): Campement {
        return jpaRepo.save(entity.toEntity()).toDomain()
    }

    override fun idOf(entity: Campement): UUID {
        return entity.ownerID
    }

    override fun deleteById(id: UUID) {
        jpaRepo.deleteById(id)
    }

    override fun isChunkClaimed(chunkCoord: ChunkCoord): Boolean {
        return territoryChunkRepository.getById(chunkCoord)!=null
    }

    override fun getCampementByChunk(chunkCoord: ChunkCoord): Campement? {
        return territoryChunkRepository.findByIdAndType(chunkCoord, CampementChunk::class.java)?.campement
    }

    override fun findAll(): List<Campement> {
        return jpaRepo.findAll().map { it.toDomain() }
    }

    override fun findnByPlayer(player: UUID): Campement? {
        return getById(player)
    }

}

@org.springframework.stereotype.Repository
interface CampementJpaRepository : JpaRepository<CampementEntity, UUID> {
}

