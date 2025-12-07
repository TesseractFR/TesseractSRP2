package onl.tesseract.srp.repository.hibernate.territory.entity

import jakarta.persistence.Cacheable
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.Table
import onl.tesseract.srp.domain.territory.TerritoryChunk

@Entity
@Table(name = "t_territory_chunks")
@Cacheable
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
abstract class TerritoryChunkEntity (
    @Id
    var id: TerritoryChunkId = TerritoryChunkId()
){
    abstract fun toDomain(): TerritoryChunk
}
