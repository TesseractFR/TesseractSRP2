package onl.tesseract.srp.controller.menu.job

import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.logger.LoggerFactory
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.util.ItemLoreBuilder
import onl.tesseract.lib.util.menu.InventoryHeadIcons
import onl.tesseract.lib.util.plus
import onl.tesseract.lib.util.toComponent
import onl.tesseract.srp.job.domain.model.Job
import onl.tesseract.srp.job.domain.model.PlayerID
import onl.tesseract.srp.job.domain.model.talenttree.*
import onl.tesseract.srp.job.domain.port.userside.JobPlayerProgressionService
import onl.tesseract.srp.job.domain.port.userside.JobTalentTreeService
import onl.tesseract.srp.util.menu.BiMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import org.slf4j.Logger
import kotlin.math.min

val logger: Logger = LoggerFactory.getLogger(JobSkillMenu::class.java)

class JobSkillMenu(
    val playerID: PlayerID, val job: Job,
    val jobTalentTreeService: JobTalentTreeService,
    val jobPlayerProgressionService: JobPlayerProgressionService
) :
    BiMenu(MenuSize.Six, "Compétences".toComponent()) {

    private lateinit var menuConfig: TalentTree
    private var scroll: Int = 0

    override fun placeButtons(viewer: Player) {

        menuConfig = try {
            jobTalentTreeService.getTalentTree(job.jobName())
        } catch (e: Exception) {
            logger.error("Failed to open skill menu for job $job", e)
            viewer.sendMessage(NamedTextColor.RED + "Une erreur est survenue lors de l'ouverture du menu. Veuillez contacter un administrateur.")
            close()
            return
        }

        openScroll(0)
    }

    private fun openScroll(scroll: Int) {
        clearTop()
        this.scroll = scroll
        val maxHeight = min(menuConfig.matrix.size, 6+scroll)
        for (row in scroll until maxHeight) {
            for(col in menuConfig.matrix[row].indices){
                val index = col + ((5 - (row - scroll)) * 9)
                val cell = menuConfig.matrix[row][col]
                placeCell(cell,index)
            }
        }

        addBottomButton(
            13, ItemBuilder(Material.PLAYER_HEAD)
                .customHead(InventoryHeadIcons.UP_ARROW.data, null)
                .name(NamedTextColor.GRAY + "Monter")
                .build()
        ) {
            if (scroll < menuConfig.matrix.size - 1)
                openScroll(scroll + 1)
        }
        addBottomButton(
            21, ItemBuilder(Material.PLAYER_HEAD)
                .customHead(InventoryHeadIcons.LEFT_ARROW_LOG.data, null)
                .name(NamedTextColor.GRAY + "Gauche")
                .build()
        )
        addBottomButton(
            23, ItemBuilder(Material.PLAYER_HEAD)
                .customHead(InventoryHeadIcons.RIGHT_ARROW_LOG.data, null)
                .name(NamedTextColor.GRAY + "Droite")
                .build()
        )
        addBottomButton(
            31, ItemBuilder(Material.PLAYER_HEAD)
                .customHead(InventoryHeadIcons.DOWN_ARROW.data, null)
                .name(NamedTextColor.GRAY + "Descendre")
                .build()
        ) {
            if (scroll > 0)
                openScroll(scroll - 1)
        }
    }

    private fun placeCell(cellType: CellType, index: Int) {
        if (cellType is Arrow) {
            addButton(
                index,
                ItemBuilder(Material.STONE_BUTTON)
                    .name(" ")
                    .customModelData(cellType.type.customModelData)
                    .build()
            )
        }

        if (cellType is RootCell) {
            addButton(
                index,
                ItemBuilder(Material.DIAMOND_PICKAXE)
                    .name(job.jobDisplayName().value())
                    .build()
            )
        }

        if (cellType is SkillCell) {
            val skill = job.talents().get(cellType.talent())
            val lore = ItemLoreBuilder()
                .newline()
                .append(skill.bonus.description)
                .newline()
            if (jobPlayerProgressionService.getTalentLevel(playerID, job.jobName(),skill.name) > 0) {
                lore.append(NamedTextColor.GREEN + "Acquis")
            } else {
                val color =
                    if (jobPlayerProgressionService.canBuyUpgrade(playerID, job.jobName(),skill)) NamedTextColor.BLUE
                    else NamedTextColor.RED
                lore.append(color + "Coût" + (NamedTextColor.GRAY + " : ${jobPlayerProgressionService.getTalentCost(playerID,job.jobName(),skill)}"))
                if (!jobPlayerProgressionService.isAvailable(playerID, job.jobName(),skill))
                    lore.newline().append(NamedTextColor.RED + "Bloqué")
            }
            addButton(
                index,
                ItemBuilder(Material.RABBIT_FOOT)
                    .name(skill.name.value)
                    .lore(lore.get())
                    .build()
            ) {
                val unlocked = jobPlayerProgressionService.upgradeSkill(playerID, skill)
                if (unlocked)
                    openScroll(this.scroll)
            }
        }
    }
}