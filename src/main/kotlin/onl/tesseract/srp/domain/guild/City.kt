package onl.tesseract.srp.domain.guild

import onl.tesseract.srp.domain.Claim
import org.bukkit.Location

class City(
    val spawnLocation: Location,
    chunks: Set<Claim> = setOf(),
) {

    private val _chunks: MutableSet<Claim> = chunks.toMutableSet()
    val chunks: Set<Claim>
        get() = _chunks

    /**
     * @throws IllegalStateException If the guild already has chunks
     */
    fun claimInitialChunks() {
        check(chunks.isEmpty())

        val spawnChunk = spawnLocation.chunk
        for (x in -1..1) {
            for (z in -1..1) {
                _chunks.add(Claim(spawnChunk.x + x, spawnChunk.z + z))
            }
        }
    }
}
