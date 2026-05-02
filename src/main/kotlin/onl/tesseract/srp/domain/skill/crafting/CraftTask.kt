package onl.tesseract.srp.domain.skill.crafting

import onl.tesseract.srp.domain.item.CustomItem
import kotlin.time.Duration

data class CraftTask(
    val queuedRecipe: QueuedRecipe,
    val done: MutableList<CustomItem>,
    val garbage: MutableList<CustomItem>,
    val unitDuration: Duration,
) {

    fun getTotalTimeLeft(): Duration {
        return unitDuration.times(queuedRecipe.quantity)
    }
}