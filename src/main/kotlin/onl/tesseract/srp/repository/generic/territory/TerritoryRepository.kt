package onl.tesseract.srp.repository.generic.territory

import onl.tesseract.lib.repository.Repository
import onl.tesseract.srp.domain.commun.ChunkCoord
import onl.tesseract.srp.domain.territory.Territory
import onl.tesseract.srp.domain.territory.TerritoryChunk
import java.util.UUID

interface TerritoryChunkRepository : Repository<TerritoryChunk, ChunkCoord>{
    fun <T : TerritoryChunk> findByIdAndType(id: ChunkCoord, type: Class<T>): T?
    fun findAllByRange(world: String, minX: Int, maxX: Int, minZ: Int, maxZ: Int): Collection<TerritoryChunk>
}

interface TerritoryRepository<T : Territory<*>,ID> : Repository<T,ID>{
    fun findnByPlayer(player: UUID): T?
}
