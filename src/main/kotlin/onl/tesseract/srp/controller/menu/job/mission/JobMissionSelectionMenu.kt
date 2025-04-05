package onl.tesseract.srp.controller.menu.job.mission

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.*
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.util.append
import onl.tesseract.srp.controller.menu.job.JobSelectionMenu
import onl.tesseract.srp.domain.job.mission.JobMission
import onl.tesseract.srp.service.job.mission.JobMissionService
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import java.util.UUID

class JobMissionSelectionMenu(
    private val playerID: UUID,
    private val missionService: JobMissionService
) : Menu(MenuSize.Hopper, Component.text("Missions de récolte", BLUE), type = InventoryType.HOPPER) {

    private val totalSlots = 5
    private val unlockedSlots = 2

    companion object {
        val playerMissions = mutableMapOf<UUID, MutableMap<Int, JobMission>>()
    }

    override fun placeButtons(viewer: Player) {
        addCloseButton()
        addBackButton()

        val missions = playerMissions.computeIfAbsent(playerID) {
            val fromDb = missionService.getMissionsForPlayer(playerID)
            val slotMap = mutableMapOf<Int, JobMission>()
            fromDb.forEachIndexed { index, mission ->
                if (index < totalSlots) slotMap[index] = mission
            }
            slotMap
        }

        repeat(totalSlots) { index ->
            if (index >= unlockedSlots) {
                addButton(index, ItemBuilder(Material.BARRIER)
                    .name(Component.text("Slot verrouillé", RED))
                    .lore()
                    .append(Component.text("Débloque ce slot en montant de niveau.", GRAY))
                    .buildLore()
                    .build()
                )
                return@repeat
            }

            val mission = missions[index]

            if (mission != null) {
                val progress = missionService.getProgress(viewer, mission)

                addButton(index, ItemBuilder(mission.material.customMaterial)
                    .name(Component.text("Mission en cours", YELLOW))
                    .lore()
                    .append(Component.text("Métier : ", GRAY).append(mission.job.name, GOLD))
                    .newline()
                    .append(Component.text("Quantité : ", GRAY)
                        .append("$progress", GREEN)
                        .append("/", GRAY)
                        .append("${mission.quantity}", RED))
                    .newline()
                    .append(Component.text("Qualité : ", GRAY)
                        .append("${mission.minimalQuality}%", AQUA))
                    .newline()
                    .append(Component.text("Récompense : ", GRAY)
                        .append("${mission.reward} lys", GREEN))
                    .newline()
                    .append(Component.text("Clique pour voir la mission", YELLOW))
                    .buildLore()
                    .build()
                ) {
                    JobMissionMenu(mission.job, mission, missionService, index, playerID, this).open(viewer)
                }
            } else {
                addButton(index, ItemBuilder(Material.STRUCTURE_VOID)
                    .name(Component.text("Sélectionner une mission", GREEN))
                    .lore()
                    .append(Component.text("Clique pour choisir un métier", GRAY))
                    .buildLore()
                    .build()
                ) {
                    JobSelectionMenu("Sélection de mission", this) { viewer, job ->
                        val newMission = missionService.createRandomMissionForJob(playerID, job)
                        missions[index] = newMission
                        JobMissionMenu(job, newMission, missionService, index, playerID, this).open(viewer)
                    }
                    .open(viewer)
                }
            }
        }

        super.placeButtons(viewer)
    }

}
