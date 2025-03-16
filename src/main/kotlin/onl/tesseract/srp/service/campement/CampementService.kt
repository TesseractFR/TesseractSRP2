package onl.tesseract.srp.service.campement

import jakarta.transaction.Transactional
import onl.tesseract.srp.domain.campement.Campement
import onl.tesseract.srp.repository.hibernate.campement.CampementRepository
import org.bukkit.Location
import org.springframework.stereotype.Service
import java.util.*

@Service
open class CampementService(private val repository: CampementRepository) {

    open fun getCampementByOwner(ownerID: UUID): Campement? {
        return repository.getByOwnerID(ownerID)
    }

    @Transactional
    open fun createCampement(ownerID: UUID, listChunks: List<String>, spawnLocation: Location) {
        val campement = Campement(
            id = UUID.randomUUID(),
            ownerID = ownerID,
            trustedPlayers = emptyList(),
            chunks = listChunks.size,
            listChunks = listChunks,
            campLevel = 1,
            spawnLocation = spawnLocation,
        )
        repository.save(campement)
    }

    @Transactional
    open fun deleteCampement(id: UUID) {
        repository.deleteById(id)
    }

    @Transactional
    open fun setSpawnpoint(ownerID: UUID, newLocation: Location) {
        val campement = repository.getByOwnerID(ownerID) ?: return
        repository.save(campement.setSpawnpoint(newLocation))
    }
}
