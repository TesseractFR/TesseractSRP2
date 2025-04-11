package onl.tesseract.srp.controller.menu.job.mission

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.*
import onl.tesseract.lib.logger.LoggerFactory
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.util.append
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.domain.job.EnumJob
import onl.tesseract.srp.service.job.mission.JobMissionService
import onl.tesseract.srp.service.job.PlayerJobService
import onl.tesseract.srp.util.jobsChatFormatError
import org.bukkit.Material
import org.bukkit.entity.Player
import org.slf4j.Logger
import java.util.*

private val logger: Logger = LoggerFactory.getLogger(JobMissionMenu::class.java)

class JobMissionMenu(
    private val job: EnumJob,
    private val missionId: Long,
    private val missionService: JobMissionService,
    private val playerID: UUID,
    private val playerJobService: PlayerJobService,
    previous: Menu? = null
) : Menu(MenuSize.Two, Component.text("Mission : ${job.name}", BLUE), previous) {

    override fun placeButtons(viewer: Player) {
        val mission = missionService.getMissionById(missionId)
            ?: return viewer.closeInventory()

        addCloseButton()
        addBackButton()

        addButton(2, ItemBuilder(Material.ANVIL)
            .name(Component.text("Métier : ", GRAY).append(job.name, GOLD))
            .lore()
            .append(Component.text("Mission générée pour ce métier", GRAY))
            .buildLore()
            .build())

        addButton(4, missionService.buildMissionButtonItem(viewer, mission, mission.material.displayName, "Clique pour déposer tes items")) {
            try {
                missionService.consumeItemsForMission(viewer, mission.id)
                placeButtons(viewer)
            } catch (e: Exception) {
                logger.error("Error while attempting to deposit items for mission $mission", e)
                viewer.sendMessage(jobsChatFormatError + "Une erreur est survenue lors du dépôt des items.")
                close()
            }
        }

        val progression = playerJobService.getPlayerJobProgression(playerID)

        addButton(6, ItemBuilder(Material.EXPERIENCE_BOTTLE)
            .name(Component.text("Progression de métier", GREEN))
            .lore()
            .append(Component.text("Niveau global : ", GRAY).append("${progression.level}", YELLOW))
            .newline()
            .append(Component.text("XP actuelle : ", GRAY).append("${progression.xp}", AQUA))
            .newline()
            .append(Component.text("Points de compétence : ", GRAY).append("${progression.skillPoints}", GOLD))
            .buildLore()
            .build()
        )
        super.placeButtons(viewer)
    }
}

