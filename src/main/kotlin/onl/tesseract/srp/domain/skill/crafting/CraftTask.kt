package onl.tesseract.srp.domain.skill.crafting

import onl.tesseract.srp.domain.item.CustomItem
import onl.tesseract.srp.domain.skill.station.CraftingStation
import kotlin.time.Duration

data class CraftTask(
    val queuedRecipe: QueuedRecipe,
    val done: MutableList<CustomItem>,
    val garbage: MutableList<CustomItem>,
    val unitDuration: Duration,
    val station: CraftingStation = CraftingStation()
) {
    private var timeLeft : Duration
    
    init {
        timeLeft = unitDuration.times(queuedRecipe.quantity)
    }
    
    fun tick(duration: Duration) {
        timeLeft -= duration
        if (timeLeft.isNegative()) {
            timeLeft = Duration.ZERO
        }
    }
    
    fun getTotalTimeLeft(): Duration {
        return timeLeft
    }
}