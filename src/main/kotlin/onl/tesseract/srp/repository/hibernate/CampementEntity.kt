package onl.tesseract.srp.repository.hibernate.campement

import jakarta.persistence.*
import onl.tesseract.srp.domain.campement.Campement
import java.util.*

@Entity
@Table(name = "t_campements")
class CampementEntity(
    @Id
    val id: UUID,

    @Column(nullable = false)
    val ownerID: UUID,

    @ElementCollection
    val trustedPlayers: List<UUID>,

    @Column(nullable = false)
    val chunks: Int,

    @ElementCollection
    val listChunks: List<String>,

    @Column(nullable = false)
    val campLevel: Int
) {
    fun toDomain(): Campement {
        return Campement(id, ownerID, trustedPlayers, chunks, listChunks, campLevel)
    }
}

fun Campement.toEntity(): CampementEntity {
    return CampementEntity(id, ownerID, trustedPlayers, chunks, listChunks, campLevel)
}
