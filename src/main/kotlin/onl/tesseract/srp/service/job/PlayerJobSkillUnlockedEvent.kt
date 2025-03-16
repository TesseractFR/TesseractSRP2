package onl.tesseract.srp.service.job

import onl.tesseract.srp.domain.job.JobSkill
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.UUID

data class PlayerJobSkillUnlockedEvent(val playerID: UUID, val unlocked: JobSkill) : Event() {
    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
