package onl.tesseract.srp.service.campement

import jakarta.transaction.Transactional
import onl.tesseract.srp.domain.campement.Campement
import onl.tesseract.srp.repository.hibernate.CampementRepository
import org.bukkit.Location
import org.springframework.stereotype.Service
import java.util.*

@Service
open class CampementService(private val repository: CampementRepository) {

    open fun getCampementByOwner(ownerID: UUID): Campement? {
        return repository.getById(ownerID)
    }

    open fun getCampementByChunk(chunk: String): Campement? {
        return repository.getCampementByChunk(chunk)
    }

    @Transactional
    open fun createCampement(ownerID: UUID, listChunks: List<String>, spawnLocation: Location): Boolean {
        for (chunk in listChunks) {
            if (repository.isChunkClaimed(chunk)) {
                return false
            }
        }
        val campement = Campement(
            id = UUID.randomUUID(),
            ownerID = ownerID,
            trustedPlayers = emptyList(),
            nbChunks = listChunks.size,
            listChunks = listChunks,
            campLevel = 1,
            spawnLocation = spawnLocation,
        )
        repository.save(campement)
        return true
    }

    @Transactional
    open fun deleteCampement(id: UUID) {
        repository.deleteById(id)
    }

    @Transactional
    open fun setSpawnpoint(ownerID: UUID, newLocation: Location) {
        val campement = repository.getById(ownerID) ?: return
        repository.save(campement.setSpawnpoint(newLocation))
    }

    enum class AnnexationResult {
        SUCCESS, ALREADY_OWNED, ALREADY_CLAIMED, NOT_ADJACENT
    }

    @Transactional
    open fun claimChunk(ownerID: UUID, chunk: String): AnnexationResult {
        val campement = repository.getById(ownerID) ?: return AnnexationResult.NOT_ADJACENT
        if (campement.listChunks.contains(chunk)) {
            return AnnexationResult.ALREADY_OWNED
        }
        if (repository.isChunkClaimed(chunk)) {
            return AnnexationResult.ALREADY_CLAIMED
        }
        val (chunkX, chunkZ) = chunk.split(",").map { it.toInt() }
        val isAdjacent = campement.listChunks.any { existingChunk ->
            val (existingX, existingZ) = existingChunk.split(",").map { it.toInt() }
            (existingX == chunkX && (existingZ == chunkZ - 1 || existingZ == chunkZ + 1)) || // Même X, Z ±1
                    (existingZ == chunkZ && (existingX == chunkX - 1 || existingX == chunkX + 1))    // Même Z, X ±1
        }
        if (!isAdjacent) {
            return AnnexationResult.NOT_ADJACENT
        }
        campement.listChunks += chunk
        campement.nbChunks = campement.listChunks.size
        repository.save(campement)
        return AnnexationResult.SUCCESS
    }

    @Transactional
    open fun unclaimChunk(ownerID: UUID, chunk: String): Boolean {
        val campement = repository.getById(ownerID) ?: return false
        if (!campement.listChunks.contains(chunk)) {
            return false
        }
        val updatedChunks = campement.listChunks.toMutableList()
        updatedChunks.remove(chunk)
        if (updatedChunks.isEmpty()) {
            repository.deleteById(campement.id)
            return true
        }
        campement.listChunks = updatedChunks
        campement.nbChunks = updatedChunks.size
        repository.save(campement)
        return true
    }


}
