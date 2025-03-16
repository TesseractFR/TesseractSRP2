package onl.tesseract.srp.service.campement

import jakarta.transaction.Transactional
import onl.tesseract.srp.domain.campement.Campement
import onl.tesseract.srp.repository.hibernate.campement.CampementRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
open class CampementService(private val repository: CampementRepository) {

    open fun getCampementByOwner(ownerID: UUID): Campement? {
        return repository.getByOwnerID(ownerID)
    }

    @Transactional
    open fun createCampement(ownerID: UUID, listChunks: List<String>) {
        val campement = Campement(
            id = UUID.randomUUID(),
            ownerID = ownerID,
            trustedPlayers = emptyList(),
            chunks = listChunks.size,
            listChunks = listChunks,
            campLevel = 1
        )
        repository.save(campement)
    }

    @Transactional
    open fun addTrustedPlayer(id: UUID, playerID: UUID) {
        val campement = repository.getById(id) ?: return
        repository.save(campement.addTrustedPlayer(playerID))
    }

    @Transactional
    open fun deleteCampement(id: UUID) {
        repository.deleteById(id)
    }
}
