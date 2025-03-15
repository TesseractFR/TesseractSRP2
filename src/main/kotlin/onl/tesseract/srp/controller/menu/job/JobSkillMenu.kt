package onl.tesseract.srp.controller.menu.job

import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.logger.LoggerFactory
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.util.menu.InventoryHeadIcons
import onl.tesseract.lib.util.plus
import onl.tesseract.lib.util.toComponent
import onl.tesseract.srp.config.JobSkillMenuConfig
import onl.tesseract.srp.config.JobSkillMenuConfigParser
import onl.tesseract.srp.domain.job.EnumJob
import org.bukkit.Material
import org.bukkit.entity.Player
import org.slf4j.Logger
import java.util.UUID

val logger: Logger = LoggerFactory.getLogger(JobSkillMenu::class.java)

class JobSkillMenu(val playerID: UUID, val job: EnumJob) : Menu(MenuSize.Six, "Compétences".toComponent()) {

    private lateinit var menuConfig: JobSkillMenuConfig

    override fun placeButtons(viewer: Player) {

        menuConfig = try {
            JobSkillMenuConfigParser().parseForJob(job)
        } catch (e: Exception) {
            logger.error("Failed to open skill menu for job $job", e)
            viewer.sendMessage(NamedTextColor.RED + "Une erreur est survenue lors de l'ouverture du menu. Veuillez contacter un administrateur.")
            close()
            return
        }

        openScroll(0)
    }

    private fun openScroll(scroll: Int) {
        clear()
        menuConfig.forEach(scroll, 6) { row, col, cellType ->
            val index = col + ((5 - (row - scroll)) * 9)
            placeCell(cellType, index)
        }

        addButton(53 - 9, ItemBuilder(Material.PLAYER_HEAD)
            .customHead(InventoryHeadIcons.UP_ARROW.data, null)
            .name(NamedTextColor.GRAY + "Monter")
            .build()) {
            if (scroll < menuConfig.cells.size - 1)
                openScroll(scroll + 1)
        }
        addButton(53, ItemBuilder(Material.PLAYER_HEAD)
            .customHead(InventoryHeadIcons.DOWN_ARROW.data, null)
            .name(NamedTextColor.GRAY + "Descendre")
            .build()) {
            if (scroll > 0)
                openScroll(scroll - 1)
        }
    }

    private fun placeCell(cellType: JobSkillMenuConfig.CellType, index: Int) {
        if (cellType is JobSkillMenuConfig.Arrow) {
            addButton(
                index,
                ItemBuilder(Material.STONE_BUTTON)
                    .name(" ")
                    .customModelData(cellType.type.customModelData)
                    .build()
            )
        }

        if (cellType is JobSkillMenuConfig.RootCell) {
            addButton(
                index,
                ItemBuilder(job.icon)
                    .name(job.displayName)
                    .build()
            )
        }

        if (cellType is JobSkillMenuConfig.SkillCell) {
            val skill = cellType.skill
            addButton(
                index,
                ItemBuilder(skill.icon)
                    .name(skill.displayName)
                    .lore("TODO") // TODO
                    .build()
            ) {
                // TODO call service
            }
        }
    }
}