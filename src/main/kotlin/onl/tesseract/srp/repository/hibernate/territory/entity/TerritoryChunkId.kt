package onl.tesseract.srp.repository.hibernate.territory.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import onl.tesseract.srp.domain.commun.ChunkCoord
import java.io.Serializable

@Embeddable
data class TerritoryChunkId(
    @Column(nullable = false)
    val world: String = "",

    @Column(nullable = false)
    val x: Int = 0,

    @Column(nullable = false)
    val z: Int = 0
) : Serializable{
    fun toDomain(): ChunkCoord{
        return ChunkCoord(x,z,world)
    }
}

fun ChunkCoord.toEntity() : TerritoryChunkId{
    return TerritoryChunkId(world,x,z)
}