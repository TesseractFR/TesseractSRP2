package onl.tesseract.srp.service.territory.guild

import onl.tesseract.srp.domain.territory.guild.GuildChunk
import onl.tesseract.srp.service.territory.TerritoryBordersManager
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import org.springframework.stereotype.Component
import java.util.*

@Component
open class GuildBorderRenderer {

    private val activeBorders = mutableMapOf<UUID, BukkitTask>()

    fun showBorders(player: Player, chunks: Collection<GuildChunk>) {
        clearBorders(player)
        activeBorders[player.uniqueId] = TerritoryBordersManager.startBorderTask(
            player = player,
            chunksProvider = { chunks },
            x = { it.chunkCoord.x },
            z = { it.chunkCoord.z }
        )
    }

    fun clearBorders(player: Player) {
        activeBorders.remove(player.uniqueId)?.cancel()
    }

    fun isShowingBorders(player: Player): Boolean = activeBorders.containsKey(player.uniqueId)
}
