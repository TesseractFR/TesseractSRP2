package onl.tesseract.srp.infrastructure.runnable.skill

import onl.tesseract.srp.domain.skill.crafting.CraftTask
import org.bukkit.scheduler.BukkitRunnable
import kotlin.time.Duration.Companion.seconds

class CraftTickRunnable(
    private val craftTask: CraftTask
) : BukkitRunnable() {

    override fun run() {
        craftTask.tick(1.seconds)
    }
}
