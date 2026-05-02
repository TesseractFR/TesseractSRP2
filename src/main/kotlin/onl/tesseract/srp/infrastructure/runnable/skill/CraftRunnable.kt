package onl.tesseract.srp.infrastructure.runnable.skill

import onl.tesseract.srp.domain.skill.Skill
import onl.tesseract.srp.domain.skill.crafting.CraftTask
import onl.tesseract.srp.service.skill.SkillService
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID

class CraftRunnable(
    private val playerUUID: UUID,
    private val skill: Skill,
    private val craftTask: CraftTask,
    private val skillService: SkillService
) : BukkitRunnable() {

    override fun run() {
        skillService.processCraftStep(playerUUID, skill, craftTask)
    }
}
