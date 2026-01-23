package onl.tesseract.srp.repository.hibernate.territory.entity.campement

import jakarta.persistence.Cacheable
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import onl.tesseract.srp.domain.territory.campement.CampementChunk
import onl.tesseract.srp.repository.hibernate.territory.entity.TerritoryChunkEntity
import onl.tesseract.srp.repository.hibernate.territory.entity.toEntity


@Entity
@Table(name = "t_campement_chunks")
@Cacheable
@DiscriminatorValue("CAMPEMENT")
class CampementChunkEntity(

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "campement_id", nullable = false)
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
