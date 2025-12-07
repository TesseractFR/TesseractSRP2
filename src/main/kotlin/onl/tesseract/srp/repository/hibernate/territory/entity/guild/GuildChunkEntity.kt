package onl.tesseract.srp.repository.hibernate.territory.entity.guild

import jakarta.persistence.Cacheable
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import onl.tesseract.srp.domain.territory.guild.GuildChunk
import onl.tesseract.srp.repository.hibernate.guild.GuildEntity
import onl.tesseract.srp.repository.hibernate.territory.entity.TerritoryChunkEntity
import onl.tesseract.srp.repository.hibernate.territory.entity.toEntity


@Entity
@Table(name = "t_guild_chunks")
@Cacheable
@DiscriminatorValue("GUILD")
class GuildChunkEntity(

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "guild_id", nullable = false)
    var guild: GuildEntity

) : TerritoryChunkEntity() {
    override fun toDomain(): GuildChunk {
        return GuildChunk(
            id.toDomain(),
            guild.toDomain()
        )
    }
}

fun GuildChunk.toEntity(guildEntity: GuildEntity): GuildChunkEntity{
    val gce = GuildChunkEntity(guildEntity)
    gce.id = chunkCoord.toEntity()
    return gce
}
