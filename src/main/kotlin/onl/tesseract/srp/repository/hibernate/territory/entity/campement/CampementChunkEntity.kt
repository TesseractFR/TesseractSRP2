package onl.tesseract.srp.repository.hibernate.territory.entity.campement

import jakarta.persistence.*
import onl.tesseract.srp.domain.territory.campement.CampementChunk
import onl.tesseract.srp.repository.hibernate.CampementEntity
import onl.tesseract.srp.repository.hibernate.territory.entity.TerritoryChunkEntity
import onl.tesseract.srp.repository.hibernate.territory.entity.toEntity


@Entity
@Table(name = "t_campement_chunks")
@Cacheable
@DiscriminatorValue("CAMPEMENT")
class CampementChunkEntity(

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ownerID", nullable = false)
    var campement: CampementEntity

) : TerritoryChunkEntity() {
    override fun toDomain(): CampementChunk {
        return CampementChunk(id.toDomain(),campement.toDomain())
    }
}

fun CampementChunk.toEntity(campement: CampementEntity): CampementChunkEntity{
    val gce = CampementChunkEntity(campement)
    gce.id = chunkCoord.toEntity()
    return gce
}