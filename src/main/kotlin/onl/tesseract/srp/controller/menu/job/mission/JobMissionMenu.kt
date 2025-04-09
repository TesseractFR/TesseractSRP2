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
import onl.tesseract.srp.domain.job.mission.JobMission
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
    private val mission: JobMission,
    private val missionService: JobMissionService,
    private val playerID: UUID,
    private val playerJobService: PlayerJobService,
    previous: Menu? = null
) : Menu(MenuSize.Two, Component.text("Mission : ${job.name}", BLUE), previous) {

    override fun placeButtons(viewer: Player) {
        try {
            addCloseButton()
            addBackButton()

            addButton(2, ItemBuilder(Material.ANVIL)
                .name(Component.text("Métier : ", GRAY).append(job.name, GOLD))
                .lore()
                .append(Component.text("Mission générée pour ce métier", GRAY))
                .buildLore()
                .build())

            addButton(4, ItemBuilder(mission.material.customMaterial)
                .name(Component.text(mission.material.displayName, YELLOW))
                .lore()
                .append(missionService.getMissionProgressComponent(viewer, mission))
                .newline()
                .append(
                    Component.text("Qualité minimale : ", GRAY)
                        .append("${mission.minimalQuality}%", AQUA)
                )
                .newline()
                .append(
                    Component.text("Récompense : ", GRAY)
                        .append("${mission.reward} lys", GREEN)
                )
                .newline()
                .append(Component.text("Clique pour déposer tes items", GOLD))
                .buildLore()
                .build()
            ) {
                try {
                    missionService.consumeItemsForMission(viewer, mission.id)
                    val updatedMission = missionService.getMissionById(mission.id)
                    if (updatedMission != null) {
                        mission.delivered = updatedMission.delivered
                        placeButtons(viewer)
                    } else {
                        viewer.closeInventory()
                    }

                } catch (e: Exception) {
                    logger.error("Error while attempting to deposit items for mission $mission", e)
                    viewer.sendMessage(jobsChatFormatError + "Une erreur est survenue lors du dépôt des items.")
                    close()
                }
            }

            val progression = playerJobService.getPlayerJobProgression(playerID)
            val level = progression.level
            val xp = progression.xp
            val skillPoints = progression.skillPoints

            addButton(6, ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .name(Component.text("Progression de métier", GREEN))
                .lore()
                .append(Component.text("Niveau global : ", GRAY).append(Component.text("$level", YELLOW)))
                .newline()
                .append(Component.text("XP actuelle : ", GRAY).append(Component.text("$xp", AQUA)))
                .newline()
                .append(Component.text("Points de compétence : ", GRAY).append(Component.text("$skillPoints", GOLD)))
                .buildLore()
                .build()
            )


            super.placeButtons(viewer)

        } catch (e: Exception) {
            logger.error("Error while displaying the mission menu for player $playerID", e)
            viewer.sendMessage(jobsChatFormatError + "Une erreur est survenue lors de l'ouverture du menu de mission.")
            close()
        }
    }
}
