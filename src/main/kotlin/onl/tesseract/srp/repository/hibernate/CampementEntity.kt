package onl.tesseract.srp.repository.hibernate

import jakarta.persistence.*
import onl.tesseract.srp.domain.territory.campement.Campement
import onl.tesseract.srp.repository.hibernate.territory.entity.campement.CampementChunkEntity
import onl.tesseract.srp.repository.hibernate.territory.entity.campement.toEntity
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
        val camp =  Campement(
            ownerID,  campLevel,
            Location(Bukkit.getWorld(spawnWorld), spawnX, spawnY, spawnZ),
            trustedPlayers
        )
        camp.addChunks(listChunks.map { it.toDomain() }.toSet())
        return camp
    }
}

fun Campement.toEntity(): CampementEntity {
     val campementEntity = CampementEntity(
        ownerID,
        trustedPlayers,
        campLevel = campLevel,
        spawnX = spawnLocation.x,
        spawnY = spawnLocation.y,
        spawnZ = spawnLocation.z,
        spawnWorld = spawnLocation.world.name
    )
    campementEntity.listChunks.addAll(getChunks().map { it.toEntity(campementEntity) }.toMutableSet())
    return campementEntity
}


