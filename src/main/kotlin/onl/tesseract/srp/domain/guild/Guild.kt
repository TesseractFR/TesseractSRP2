package onl.tesseract.srp.domain.guild

import onl.tesseract.srp.domain.campement.CampementChunk
import org.bukkit.Location
import java.util.*

class Guild(
    val id: Int,
    val leaderId: UUID,
    val name: String,
    val spawnLocation: Location,
    chunks: Set<CampementChunk> = setOf()
) {

    private val _chunks: MutableSet<CampementChunk> = chunks.toMutableSet()
    val chunks: Set<CampementChunk>
        get() = _chunks

    /**
     * @throws IllegalStateException If the guild already has chunks
     */
    fun claimInitialChunks() {
        check(chunks.isEmpty())

        val spawnChunk = spawnLocation.chunk
        for (x in -1 .. 1) {
            for (z in -1 .. 1) {
                _chunks.add(CampementChunk(spawnChunk.x + x, spawnChunk.z + z))
            }
        }
    }
}