package onl.tesseract.srp.repository.hibernate.territory.entity.guild

import jakarta.persistence.*
import onl.tesseract.srp.domain.territory.guild.GuildChunk
import onl.tesseract.srp.repository.hibernate.guild.GuildEntity
import onl.tesseract.srp.repository.hibernate.territory.entity.TerritoryChunkEntity
import onl.tesseract.srp.repository.hibernate.territory.entity.toEntity


@Entity
@Table(name = "t_guild_chunks")
@Cacheable
@DiscriminatorValue("GUILD")
class GuildChunkEntity(

    @ManyToOne(fetch = FetchType.LAZY)
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