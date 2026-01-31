package onl.tesseract.srp.repository.generic.territory

import onl.tesseract.srp.domain.commun.ChunkCoord
import onl.tesseract.srp.domain.territory.campement.Campement
import java.util.UUID

interface CampementRepository : TerritoryRepository<Campement, UUID> {
    fun deleteById(id: UUID)
    fun isChunkClaimed(chunkCoord: ChunkCoord): Boolean
    fun getCampementByChunk(chunkCoord: ChunkCoord): Campement?
    fun findAll(): List<Campement>
}