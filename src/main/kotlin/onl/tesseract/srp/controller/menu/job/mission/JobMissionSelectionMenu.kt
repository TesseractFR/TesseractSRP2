package onl.tesseract.srp.controller.menu.job.mission

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.*
import onl.tesseract.lib.logger.LoggerFactory
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.util.append
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

    companion object {
        val playerMissions = mutableMapOf<UUID, MutableMap<Int, JobMission>>()
    }

    override fun placeButtons(viewer: Player) {
        try {
            addCloseButton()
            addBackButton()

            val unlockedSlots = getUnlockedSlots(playerID)
            val missions = loadPlayerMissions(viewer)

            repeat(totalSlots) { index ->
                if (index >= unlockedSlots) {
                    displayLockedSlot(index)
                } else {
                    val mission = missions[index]
                    if (mission != null) {
                        displayOngoingMission(viewer, mission, index)
                    } else {
                        displayEmptySlot(viewer, index, missions)
                    }
                }
            }

            super.placeButtons(viewer)

        } catch (e: Exception) {
            logger.error("Unexpected error in JobMissionSelectionMenu for player $playerID", e)
            viewer.sendMessage(jobsChatFormatError + "Une erreur est survenue lors de l'ouverture du menu de missions.")
            close()
        }
    }
    private fun loadPlayerMissions(viewer: Player): MutableMap<Int, JobMission> {
        return playerMissions.computeIfAbsent(playerID) {
            try {
                val fromDb = missionService.getMissionsForPlayer(playerID)
                val slotMap = mutableMapOf<Int, JobMission>()
                fromDb.forEachIndexed { index, mission ->
                    if (index < totalSlots) slotMap[index] = mission
                }
                slotMap
            } catch (e: Exception) {
                logger.error("Failed to load missions for player $playerID", e)
                viewer.sendMessage(jobsChatFormatError + "Une erreur est survenue lors du chargement de tes missions.")
                mutableMapOf()
            }
        }
    }

    private fun displayLockedSlot(index: Int) {
        val requiredRank = getRequiredRankForSlot(index)
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
        try {
            addButton(index, ItemBuilder(mission.material.customMaterial)
                .name(Component.text("Mission en cours", YELLOW))
                .lore()
                .append(Component.text("Métier : ", GRAY).append(mission.job.name, GOLD))
                .newline()
                .append(missionService.getMissionProgressComponent(viewer, mission))
                .newline()
                .append(Component.text("Qualité minimale : ", GRAY).append("${mission.minimalQuality}%", AQUA))
                .newline()
                .append(Component.text("Récompense : ", GRAY).append("${mission.reward} lys", GREEN))
                .newline()
                .append(Component.text("Clique pour voir la mission", YELLOW))
                .buildLore()
                .build()
            ) {
                try {
                    JobMissionMenu(mission.job, mission, missionService, playerID, playerJobService, this).open(viewer)
                } catch (e: Exception) {
                    logger.error("Failed to open mission menu for mission $mission", e)
                    viewer.sendMessage(jobsChatFormatError + "Impossible d'ouvrir cette mission.")
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to display mission $mission for player $playerID", e)
        }
    }

    private fun displayEmptySlot(viewer: Player, index: Int, missions: MutableMap<Int, JobMission>) {
        addButton(index, ItemBuilder(Material.STRUCTURE_VOID)
            .name(Component.text("Sélectionner une mission", GREEN))
            .lore()
            .append(Component.text("Clique pour choisir un métier", GRAY))
            .buildLore()
            .build()
        ) {
            try {
                JobSelectionMenu("Sélection de mission", this) { viewer, job ->
                    handleJobSelection(viewer, job, index, missions)
                }.open(viewer)
            } catch (e: Exception) {
                logger.error("Failed to open job selection menu for player $playerID", e)
                viewer.sendMessage(jobsChatFormatError + "Impossible d'ouvrir le menu de sélection de métier.")
            }
        }
    }

    private fun handleJobSelection(viewer: Player, job: EnumJob, index: Int, missions: MutableMap<Int, JobMission>) {
        try {
            val success = missionService.createRandomMissionForJob(playerID, job)
            if (success) {
                val refreshed = missionService.getMissionsForPlayer(playerID)
                    .firstOrNull { it.job == job && !missions.containsValue(it) }

                if (refreshed != null) {
                    missions[index] = refreshed
                    JobMissionMenu(job, refreshed, missionService, playerID, playerJobService, this).open(viewer)
                } else {
                    viewer.sendMessage(jobsChatFormatError + "Impossible de retrouver la mission créée.")
                }
            } else {
                viewer.sendMessage(jobsChatFormatError + "Une erreur est survenue lors de la création de la mission.")
            }
        } catch (e: Exception) {
            logger.error("Failed to create a mission for job $job (player: $playerID)", e)
            viewer.sendMessage(jobsChatFormatError + "Une erreur est survenue lors de la création de la mission.")
        }
    }

    private fun getUnlockedSlots(playerID: UUID): Int {
        return when (playerService.getPlayer(playerID).rank) {
            PlayerRank.Survivant -> 1
            PlayerRank.Explorateur,
            PlayerRank.Aventurier -> 2
            PlayerRank.Noble,
            PlayerRank.Baron,
            PlayerRank.Seigneur -> 3
            PlayerRank.Vicomte,
            PlayerRank.Comte,
            PlayerRank.Duc -> 4
            PlayerRank.Roi,
            PlayerRank.Empereur -> 5
        }
    }

    private fun getRequiredRankForSlot(slot: Int): PlayerRank {
        return when (slot) {
            0 -> PlayerRank.Survivant
            1 -> PlayerRank.Explorateur
            2 -> PlayerRank.Noble
            3 -> PlayerRank.Vicomte
            4 -> PlayerRank.Roi
            else -> PlayerRank.Empereur
        }
    }

}
