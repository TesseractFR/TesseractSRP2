package onl.tesseract.srp.repository.hibernate.territory

import onl.tesseract.srp.domain.territory.ChunkCoord
import onl.tesseract.srp.domain.territory.TerritoryChunk
import onl.tesseract.srp.repository.generic.territory.TerritoryChunkRepository
import onl.tesseract.srp.repository.hibernate.guild.GuildEntity
import onl.tesseract.srp.repository.hibernate.territory.entity.TerritoryChunkEntity
import onl.tesseract.srp.repository.hibernate.territory.entity.TerritoryChunkId
import onl.tesseract.srp.repository.hibernate.territory.entity.toEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TerritoryChunkJpaRepository : JpaRepository<TerritoryChunkEntity, TerritoryChunkId>{
    @Query("""
        SELECT t
        FROM TerritoryChunkEntity t
        WHERE t.id.world = :world
          AND t.id.x BETWEEN :minX AND :maxX
          AND t.id.z BETWEEN :minZ AND :maxZ
    """)
    fun findChunksInArea(
        @Param("world") world: String,
        @Param("minX") minX: Int,
        @Param("maxX") maxX: Int,
        @Param("minZ") minZ: Int,
        @Param("maxZ") maxZ: Int
    ): List<TerritoryChunkEntity>
}

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

    override fun findAllByRange(
        world: String,
        minX: Int,
        maxX: Int,
        minZ: Int,
        maxZ: Int,
    ): Collection<TerritoryChunk> {
        return territoryChunkJpaRepository.findChunksInArea(world, minX, maxX, minZ, maxZ)
                .map { it.toDomain() }
    }

}

fun TerritoryChunk.toEntity() : TerritoryChunkEntity {
    throw IllegalArgumentException("Pas de cast possible sur TerritoryChunk")
}
