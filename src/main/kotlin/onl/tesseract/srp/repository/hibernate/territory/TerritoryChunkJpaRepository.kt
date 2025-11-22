package onl.tesseract.srp.repository.hibernate.territory

import onl.tesseract.srp.domain.territory.ChunkCoord
import onl.tesseract.srp.domain.territory.TerritoryChunk
import onl.tesseract.srp.repository.generic.territory.TerritoryChunkRepository
import onl.tesseract.srp.repository.hibernate.territory.entity.TerritoryChunkEntity
import onl.tesseract.srp.repository.hibernate.territory.entity.TerritoryChunkId
import onl.tesseract.srp.repository.hibernate.territory.entity.toEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Repository
interface TerritoryChunkJpaRepository : JpaRepository<TerritoryChunkEntity, TerritoryChunkId>

@Component
class TerritoryJpaRepositoryAdapter(private val territoryChunkJpaRepository: TerritoryChunkJpaRepository) : TerritoryChunkRepository {
    override fun save(entity: TerritoryChunk): TerritoryChunk {
        return territoryChunkJpaRepository.save(entity.toEntity()).toDomain()
    }

    override fun getById(id: ChunkCoord): TerritoryChunk? {
        val territoryChunkEntity = territoryChunkJpaRepository.findById(id.toEntity()).orElse(null)
        return territoryChunkEntity?.toDomain()
    }

    override fun idOf(entity: TerritoryChunk): ChunkCoord {
        return entity.chunkCoord
    }

    override fun <T : TerritoryChunk> findByIdAndType(id: ChunkCoord, type: Class<T>): T? {
        val entity = territoryChunkJpaRepository.findById(id.toEntity()).orElse(null) ?: return null
        return if (type.isInstance(entity.toDomain())) type.cast(entity.toDomain()) else null
    }

}

fun TerritoryChunk.toEntity() : TerritoryChunkEntity {
    throw IllegalArgumentException("Pas de cast possible sur TerritoryChunk")
}
