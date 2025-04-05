package onl.tesseract.srp.controller.menu.job.mission

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.*
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.util.append
import onl.tesseract.srp.controller.menu.job.JobSelectionMenu
import onl.tesseract.srp.domain.job.EnumJob
import onl.tesseract.srp.domain.job.mission.JobMission
import onl.tesseract.srp.service.job.mission.JobMissionService
import onl.tesseract.srp.controller.menu.job.mission.JobMissionSelectionMenu.Companion.playerMissions
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.*

class JobMissionMenu(
    private val job: EnumJob,
    private val mission: JobMission,
    private val missionService: JobMissionService,
    private val slotIndex: Int,
    private val playerID: UUID,
    previous: Menu? = null
) : Menu(MenuSize.Two, Component.text("Mission : ${job.name}", GOLD), previous) {

    override fun placeButtons(viewer: Player) {
        addCloseButton()
        addBackButton()

        val progress = missionService.getProgress(viewer, mission)

        addButton(2, ItemBuilder(Material.ANVIL)
            .name(Component.text("Métier : ", GRAY).append(job.name, GOLD))
            .lore()
            .append(Component.text("Mission générée pour ce métier", GRAY))
            .buildLore()
            .build())

        addButton(4, ItemBuilder(mission.material.customMaterial)
            .name(Component.text(mission.material.displayName, YELLOW))
            .lore()
            .append(
                Component.text("Quantité requise : ", GRAY)
                    .append("$progress", GREEN)
                    .append("/", GRAY)
                    .append("${mission.quantity}", RED)
            )
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
            .buildLore()
            .build()
        ) {
            missionService.consumeItemsForMission(viewer, mission)
            val updatedProgress = missionService.getProgress(viewer, mission)

            if (updatedProgress >= mission.quantity) {
                viewer.sendMessage(Component.text("Mission complétée !", GREEN))
                viewer.playSound(viewer.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                missionService.deleteMission(mission)
                playerMissions[playerID]?.remove(slotIndex)
                viewer.closeInventory()
            } else {
                val remaining = mission.quantity - updatedProgress
                viewer.sendMessage(
                    Component.text("Il manque encore ", YELLOW)
                        .append("$remaining ", RED)
                        .append(mission.material.displayName, GOLD)
                        .append(" pour terminer cette mission.", YELLOW)
                )
                JobMissionMenu(job, mission, missionService, slotIndex, playerID).open(viewer)
                viewer.closeInventory()
            }
        }

        addButton(6, ItemBuilder(Material.EXPERIENCE_BOTTLE)
            .name(Component.text("À venir...", GRAY))
            .build()
        )

        super.placeButtons(viewer)
    }
}
