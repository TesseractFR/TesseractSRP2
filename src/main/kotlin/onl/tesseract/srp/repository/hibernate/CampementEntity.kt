package onl.tesseract.srp.repository.hibernate

import jakarta.persistence.*
import onl.tesseract.srp.domain.campement.Campement
import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.*

@Entity
@Table(name = "t_campements", indexes = [
    Index(name = "idx_campement_chunks", columnList = "nbChunks")
])
class CampementEntity(
    @Id
    val ownerID: UUID,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "t_campements_trusted_players",
        joinColumns = [JoinColumn(name = "ownerID")])
    @Column(name = "trusted_player")
    val trustedPlayers: List<UUID>,

    @Column(nullable = false)
    val nbChunks: Int,

    @ElementCollection
    @CollectionTable(
        name = "t_campements_chunks",
        joinColumns = [JoinColumn(name = "ownerID")],
        indexes = [Index(name = "idx_campement_chunk", columnList = "chunks")]
    )
    @Column(name = "chunks")
    val listChunks: List<String>,

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
        return Campement(ownerID, ownerID, trustedPlayers, nbChunks, listChunks, campLevel,
            Location(Bukkit.getWorld(spawnWorld), spawnX, spawnY, spawnZ))
    }
}

fun Campement.toEntity(): CampementEntity {
    return CampementEntity(ownerID, trustedPlayers, nbChunks, listChunks, campLevel, spawnLocation.x, spawnLocation.y, spawnLocation.z, spawnLocation.world.name)
}
