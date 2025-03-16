package onl.tesseract.srp.repository.hibernate.campement

import jakarta.persistence.*
import onl.tesseract.srp.domain.campement.Campement
import java.util.*

@Entity
@Table(name = "t_campements", indexes = [
    Index(name = "idx_campement_chunks", columnList = "chunks")
])
class CampementEntity(
    @Id
    val ownerID: UUID,

    @ElementCollection
    @CollectionTable(name = "t_campements_trusted_players", joinColumns = [JoinColumn(name = "ownerID")])
    @Column(name = "trusted_player")
    val trustedPlayers: List<UUID>,

    @Column(nullable = false)
    val chunks: Int,

    @ElementCollection
    @CollectionTable(
        name = "t_campements_chunks",
        joinColumns = [JoinColumn(name = "ownerID")],
        indexes = [Index(name = "idx_campement_chunk", columnList = "chunk")]
    )
    @Column(name = "chunk")
    val listChunks: List<String>,

    @Column(nullable = false)
    val campLevel: Int
) {
    fun toDomain(): Campement {
        return Campement(ownerID, ownerID, trustedPlayers, chunks, listChunks, campLevel)
    }
}

fun Campement.toEntity(): CampementEntity {
    return CampementEntity(ownerID, trustedPlayers, chunks, listChunks, campLevel)
}
