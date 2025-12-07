package onl.tesseract.srp.repository.hibernate

import jakarta.persistence.*
import onl.tesseract.srp.domain.commun.ChunkCoord
import onl.tesseract.srp.domain.commun.Coordinate
import onl.tesseract.srp.domain.territory.campement.Campement
import onl.tesseract.srp.domain.territory.campement.CampementChunk
import onl.tesseract.srp.repository.hibernate.territory.entity.campement.CampementChunkEntity
import onl.tesseract.srp.repository.hibernate.territory.entity.campement.toEntity
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.JdbcTypeCode
import java.sql.Types
import java.util.*
import kotlin.math.floor

private const val CHUNK_SIZE = 16

@Entity
@Table(name = "t_campements")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
class CampementEntity(

    @Id
    @Column(name = "id", length = 36, columnDefinition = "VARCHAR(36)")
    @JdbcTypeCode(Types.VARCHAR)
    val id: UUID,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "t_campements_trusted_players",
        joinColumns = [JoinColumn(name = "id")]
    )
    @Column(name = "trusted_player")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    val trustedPlayers: Set<UUID>,

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "campement", orphanRemoval = true, fetch = FetchType.EAGER)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    val listChunks: MutableSet<CampementChunkEntity> = mutableSetOf(),

    @Column(nullable = false)
    val campLevel: Int,

    @Column(name = "spawn_x", nullable = false)
    val spawnX: Double,

    @Column(name = "spawn_y", nullable = false)
    val spawnY: Double,

    @Column(name = "spawn_z", nullable = false)
    val spawnZ: Double,

    @Column(name = "spawn_world", nullable = false)
    val spawnWorld: String
) {
    fun toDomain(): Campement {
        val camp =  Campement(
            id,  campLevel,
            Coordinate(spawnX, spawnY, spawnZ,
                ChunkCoord(floor(spawnX/CHUNK_SIZE).toInt(),floor(spawnZ/CHUNK_SIZE).toInt(), spawnWorld)),
            trustedPlayers.toMutableSet()
        )
        camp.addChunks(listChunks.map { CampementChunk(it.id.toDomain(),camp) }.toSet())
        return camp
    }
}

fun Campement.toEntity(): CampementEntity {
     val campementEntity = CampementEntity(
        ownerID,
        getTrusted().toSet(),
        campLevel = campLevel,
        spawnX = getSpawnpoint().x,
        spawnY = getSpawnpoint().y,
        spawnZ = getSpawnpoint().z,
        spawnWorld = getSpawnpoint().chunkCoord.world
    )
    campementEntity.listChunks.addAll(getChunks().map { it.toEntity(campementEntity) }.toMutableSet())
    return campementEntity
}


