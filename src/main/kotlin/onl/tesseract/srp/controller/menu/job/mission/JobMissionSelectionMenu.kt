package onl.tesseract.srp.controller.menu.job.mission

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.*
import onl.tesseract.lib.logger.LoggerFactory
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.controller.menu.job.JobSelectionMenu
import onl.tesseract.srp.domain.job.EnumJob
import onl.tesseract.srp.domain.job.mission.JobMission
import onl.tesseract.srp.domain.player.PlayerRank
import onl.tesseract.srp.service.job.PlayerJobService
import onl.tesseract.srp.service.job.mission.JobMissionService
import onl.tesseract.srp.service.player.SrpPlayerService
import onl.tesseract.srp.util.jobsChatFormatError
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.slf4j.Logger
import java.util.UUID

private val logger: Logger = LoggerFactory.getLogger(JobMissionSelectionMenu::class.java)

class JobMissionSelectionMenu(
    private val playerID: UUID,
    private val missionService: JobMissionService,
    private val playerService: SrpPlayerService,
    private val playerJobService: PlayerJobService
) : Menu(MenuSize.Hopper, Component.text("Missions de récolte", BLUE), type = InventoryType.HOPPER) {

    private val totalSlots = 5

    override fun placeButtons(viewer: Player) {
        addCloseButton()
        addBackButton()

        val unlockedSlots = PlayerRank.getUnlockedSlotsForRank(playerService.getPlayer(playerID).rank)
        val missions = loadPlayerMissions()

        repeat(totalSlots) { index ->
            if (index >= unlockedSlots) {
                displayLockedSlot(index)
            } else {
                val mission = missions[index]
                if (mission != null) {
                    displayOngoingMission(viewer, mission, index)
                } else {
                    displayEmptySlot(viewer, index)
                }
            }
        }

        super.placeButtons(viewer)
    }
    private fun loadPlayerMissions(): MutableMap<Int, JobMission> {
        val fromDb = missionService.getMissionsForPlayer(playerID)
        val slotMap = mutableMapOf<Int, JobMission>()
        fromDb.forEachIndexed { index, mission ->
            if (index < totalSlots) slotMap[index] = mission
        }
        return slotMap
    }

    private fun displayLockedSlot(index: Int) {
        val requiredRank = PlayerRank.getRequiredRankForSlot(index)
        addButton(index, ItemBuilder(Material.BARRIER)
            .name(Component.text("Slot verrouillé", RED))
            .lore()
            .append(Component.text("Débloqué à partir du grade : ", GRAY))
            .append(Component.text(requiredRank.name, GOLD))
            .buildLore()
            .build()
        )
    }

    private fun displayOngoingMission(viewer: Player, mission: JobMission, index: Int) {
        addButton(index, missionService.buildMissionButtonItem(viewer, mission, "Mission en cours", "Clique pour voir la mission")) {
            JobMissionMenu(mission.job, mission.id, missionService, playerID, playerJobService, this).open(viewer)
        }
    }

    private fun displayEmptySlot(viewer: Player, index: Int) {
        addButton(index, ItemBuilder(Material.STRUCTURE_VOID)
            .name(Component.text("Sélectionner une mission", GREEN))
            .lore()
            .append(Component.text("Clique pour choisir un métier", GRAY))
            .buildLore()
            .build()
        ) {
            JobSelectionMenu("Sélection de mission", this) { viewer, job ->
                handleJobSelection(viewer, job)
            }.open(viewer)
        }
    }

    private fun handleJobSelection(viewer: Player, job: EnumJob) {
        try {
            val mission = missionService.createRandomMissionForJob(playerID, job)
            JobMissionMenu(job, mission.id, missionService, playerID, playerJobService, this).open(viewer)
        } catch (e: Exception) {
            logger.error("Failed to create mission for player $playerID and job $job", e)
            viewer.sendMessage(jobsChatFormatError + "Une erreur est survenue lors de la création de la mission.")
        }
    }

}
