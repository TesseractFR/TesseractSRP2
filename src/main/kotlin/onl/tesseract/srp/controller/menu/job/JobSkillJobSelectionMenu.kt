package onl.tesseract.srp.controller.menu.job

import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.util.toComponent
import onl.tesseract.srp.domain.job.EnumJob
import onl.tesseract.srp.service.job.PlayerJobService
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.UUID

class JobSkillJobSelectionMenu(private val playerID: UUID, private val playerJobService: PlayerJobService) : Menu(MenuSize.Two, "Métiers".toComponent()) {

    override fun placeButtons(viewer: Player) {
        addBackButton()
        addCloseButton()

        EnumJob.entries.forEachIndexed { index, enumJob ->
            addButton(index, ItemBuilder(Material.DIAMOND_PICKAXE)
                .name(enumJob.name)
                .build()) {
                JobSkillMenu(playerID, enumJob, playerJobService).open(viewer)
            }
        }

        super.placeButtons(viewer)
    }
}