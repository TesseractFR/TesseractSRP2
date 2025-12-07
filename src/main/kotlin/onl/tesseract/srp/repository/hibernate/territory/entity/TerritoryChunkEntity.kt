package onl.tesseract.srp.repository.hibernate.territory.entity

import jakarta.persistence.*
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
