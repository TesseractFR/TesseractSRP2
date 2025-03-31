package onl.tesseract.srp.controller.menu.job.mission

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.TextDecoration
import onl.tesseract.lib.logger.LoggerFactory
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.util.Util
import onl.tesseract.lib.util.append
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.controller.menu.job.JobSelectionMenu
import onl.tesseract.srp.domain.job.EnumJob
import onl.tesseract.srp.domain.job.mission.JobMission
import onl.tesseract.srp.domain.player.PlayerRank
import onl.tesseract.srp.service.item.CustomItemService
import onl.tesseract.srp.service.job.mission.JobMissionService
import onl.tesseract.srp.service.player.SrpPlayerService
import onl.tesseract.srp.util.jobsChatFormat
import onl.tesseract.srp.util.jobsChatFormatError
import onl.tesseract.srp.util.jobsChatFormatSuccess
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.slf4j.Logger
import java.util.*

private val logger: Logger = LoggerFactory.getLogger(JobMissionMenu::class.java)

class JobMissionMenu(
    private val playerID: UUID,
    private val missionService: JobMissionService,
    private val playerService: SrpPlayerService,
    private val customItemService: CustomItemService
) : Menu(MenuSize.Hopper, Component.text("Missions de récolte", BLUE), type = InventoryType.HOPPER) {

    private val totalSlots = 5

    override fun placeButtons(viewer: Player) {
        addCloseButton()
        addBackButton()

        val srpPlayer = playerService.getPlayer(playerID)
        val missions = missionService.getMissionsForPlayer(playerID)

        repeat(totalSlots) { index ->
            if (index >= srpPlayer.rank.jobMissionSlots) {
                displayLockedSlot(index)
            } else if (index < missions.size) {
                val mission = missions[index]
                displayOngoingMission(viewer, mission, index)
            } else {
                displayEmptySlot(viewer, index)
            }
        }
    }

    private fun displayLockedSlot(index: Int) {
        val requiredRank = PlayerRank.getRequiredRankForMissionSlot(index)
        addButton(index, ItemBuilder(Material.BARRIER)
            .name(Component.text("Slot verrouillé", RED))
            .lore()
            .append(Component.text("Débloqué à partir du grade : ", GRAY))
            .newline()
            .append(Component.text(requiredRank.name, GOLD))
            .buildLore()
            .build()
        )
    }

    private fun displayOngoingMission(viewer: Player, mission: JobMission, index: Int) {
        addButton(
            index,
            buildMissionButtonItem(viewer, mission)
        ) {
            val (delivered, remaining) = missionService.consumeItemsForMission(viewer, mission.id)
            if (remaining == 0) {
                viewer.sendMessage(
                    jobsChatFormatSuccess
                            + "✔ Mission complétée ! Tu as gagné "
                            + Component.text("${mission.reward} lys ", GOLD)
                            + "!"
                )
            } else if (delivered > 0) {
                viewer.sendMessage(
                    jobsChatFormat + "Tu as déposé " +
                            Component.text("$delivered", YELLOW) + " " +
                            Component.text(mission.material.displayName, GOLD) + "."
                )
                viewer.sendMessage(
                    jobsChatFormat + "Il te reste " +
                            Component.text("$remaining", YELLOW) + " " +
                            Component.text(mission.material.displayName, GOLD) +
                            " à déposer pour compléter la mission."
                )
            } else {
                viewer.sendMessage(
                    jobsChatFormatError + "Tu n'as pas de " +
                            Component.text(mission.material.displayName, GOLD) +
                            " sur toi."
                )
            }
            close()
        }
    }

    private fun buildMissionButtonItem(viewer: Player, mission: JobMission): ItemStack {
        val progress = mission.delivered
        val inventoryAmount = viewer.inventory.contents
            .filterNotNull()
            .mapNotNull { runCatching { customItemService.getCustomItemStack(it) }.getOrNull() }
            .filter { it.item.material == mission.material && it.item.quality >= mission.minimalQuality }
            .sumOf { it.amount }

        val total = progress + inventoryAmount
        val gradientColor = Util.getGreenRedGradient(total, mission.quantity)

        return ItemBuilder(mission.material.customMaterial)
            .name(Component.text(mission.material.displayName, DARK_AQUA)) // Mettre la couleur de la rareté
            .lore()
            .append(Component.text("Métier : ", GRAY).append(mission.job.name, GOLD))
            .newline()
            .append(Component.text("Qualité minimale : ", GRAY).append("${mission.minimalQuality}%", AQUA))
            .newline()
            .append(Component.text("Récompense : ", GRAY).append("${mission.reward} lys", GREEN))
            .newline()
            .newline()
            .append(Component.text("Quantité déposée : ", GRAY).append("$progress", YELLOW))
            .newline()
            .append(Component.text("Quantité dans l'inventaire : ", GRAY).append("$inventoryAmount", YELLOW))
            .newline()
            .append(
                Component.text("Total : ", GRAY, TextDecoration.BOLD).append("$total", gradientColor)
                    .append(" / ${mission.quantity}", GRAY)
            )
            .newline()
            .append(Component.text("Clique pour déposer tes items", GOLD, TextDecoration.ITALIC))
            .buildLore()
            .build()
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
        missionService.createRandomMissionForJob(playerID, job)
        open(viewer)
    }
}
