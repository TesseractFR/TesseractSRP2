package onl.tesseract.srp.repository.hibernate.guild

import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import onl.tesseract.srp.domain.campement.CampementChunk
import onl.tesseract.srp.domain.guild.Guild
import org.bukkit.Bukkit
import org.bukkit.Location
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.util.*

@Entity
@Table(
    name = "t_guilds", indexes = [
        Index(columnList = "name", unique = true)
    ]
)
class GuildEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int,
    val name: String,
    val leaderId: UUID,
    @Embedded
    val spawnLocation: SpawnLocationEntity,
    @OneToMany
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    val chunks: MutableSet<GuildCityChunkEntity>
) {

    @Embeddable
    class SpawnLocationEntity(
        val spawnX: Int,
        val spawnY: Int,
        val spawnZ: Int,
    ) {

        fun toLocation(): Location {
            return Location(Bukkit.getWorld("guildWorld"), spawnX.toDouble(), spawnY.toDouble(), spawnZ.toDouble())
        }
    }

    fun toDomain(): Guild {
        return Guild(
            id,
            leaderId,
            name,
            spawnLocation.toLocation(),
            chunks.map { CampementChunk(it.x, it.z) }.toSet()
        )
    }
}

@Entity
class GuildCityChunkEntity(@Id val x: Int, @Id val z: Int)

fun Guild.toEntity(): GuildEntity {
    return GuildEntity(
        id,
        name,
        leaderId,
        GuildEntity.SpawnLocationEntity(spawnLocation.blockX, spawnLocation.blockY, spawnLocation.blockZ),
        chunks.map { GuildCityChunkEntity(it.x, it.z) }.toMutableSet()
    )
}