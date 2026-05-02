package onl.tesseract.srp.infrastructure.scheduler.skill

import onl.tesseract.srp.domain.skill.Skill
import onl.tesseract.srp.domain.skill.crafting.CraftTask
import onl.tesseract.srp.infrastructure.runnable.skill.CraftRunnable
import onl.tesseract.srp.infrastructure.runnable.skill.CraftTickRunnable
import onl.tesseract.srp.service.skill.SkillService
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import org.springframework.stereotype.Component
import java.util.*

@Component
class CraftTaskScheduler(
    private val plugin: Plugin
) {
    private val activeTasks = mutableMapOf<UUID, MutableMap<String, Pair<BukkitTask, BukkitTask>>>()

    fun schedule(player: UUID, skill: Skill, craftTask: CraftTask, skillService: SkillService) {
        val runnable = CraftRunnable(player, skill, craftTask, skillService)
        val ticks = (craftTask.unitDuration.inWholeMilliseconds / 50)
        val task = runnable.runTaskTimer(plugin, ticks, ticks)

        val tickRunnable = CraftTickRunnable(craftTask)
        val tickTask = tickRunnable.runTaskTimer(plugin, 20L, 20L)
        
        cancel(player, skill.name)
        activeTasks.getOrPut(player) { mutableMapOf() }[skill.name] = task to tickTask
    }

    fun cancel(player: UUID, skillName: String) {
        val tasks = activeTasks[player]?.remove(skillName)
        tasks?.first?.cancel()
        tasks?.second?.cancel()
        if (tasks != null) {
            Bukkit.getPlayer(player)?.sendMessage("§aFabrication de ${skillName} terminée !")
        }
    }
}
