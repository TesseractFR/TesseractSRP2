package onl.tesseract.srp.repository.hibernate

import jakarta.persistence.*
import onl.tesseract.srp.domain.campement.Campement
import onl.tesseract.srp.domain.campement.CampementChunk
import org.bukkit.Bukkit
import org.bukkit.Location
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.util.*

@Entity
@Table(name = "t_campements")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
class CampementEntity(
    @Id
    val ownerID: UUID,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "t_campements_trusted_players",
        joinColumns = [JoinColumn(name = "ownerID")]
    )
    @Column(name = "trusted_player")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    val trustedPlayers: Set<UUID>,

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id", referencedColumnName = "ownerID")
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
        return Campement(
            ownerID, trustedPlayers, listChunks.map { it.toDomain() }.toSet(), campLevel,
            Location(Bukkit.getWorld(spawnWorld), spawnX, spawnY, spawnZ)
        )
    }
}

fun Campement.toEntity(): CampementEntity {
    return CampementEntity(
        ownerID,
        trustedPlayers,
        chunks.map { it.toEntity() }.toMutableSet(),
        campLevel,
        spawnLocation.x,
        spawnLocation.y,
        spawnLocation.z,
        spawnLocation.world.name
    )
}

@Entity
@Table(name = "t_campement_chunks")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
data class CampementChunkEntity(
    @Id
    val x: Int = 0,
    @Id
    val z: Int = 0
) {

    fun toDomain(): CampementChunk = CampementChunk(x, z)
}

fun CampementChunk.toEntity(): CampementChunkEntity {
    return CampementChunkEntity(x, z)
}
