package onl.tesseract.srp.domain.skill

import org.bukkit.Location
import org.bukkit.inventory.ItemStack
import java.util.UUID

data class CraftTask(
    val playerUUID: UUID,
    val outputChest: Location,
    val duration: Long, // en ticks
    var progress: Long = 0L,
    var amountLeft : Int,
    val item: ItemStack
) {
    val isFinished: Boolean
        get() = progress >= duration;
    val percentDone: Float
        get() = (progress*1.0f)/duration
}
