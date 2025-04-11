package onl.tesseract.srp.controller.menu.job

import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.util.toComponent
import onl.tesseract.srp.domain.job.EnumJob
import org.bukkit.Material
import org.bukkit.entity.Player

class JobSelectionMenu(
    title: String,
    previous: Menu? = null,
    private val onJobClick: (viewer: Player, job: EnumJob) -> Unit
) : Menu(MenuSize.Two, title.toComponent(), previous) {

    override fun placeButtons(viewer: Player) {
        addBackButton()
        addCloseButton()

        EnumJob.entries.forEachIndexed { index, enumJob ->
            addButton(index, ItemBuilder(Material.DIAMOND_PICKAXE)
                .name(enumJob.name)
                .build()) {
                onJobClick(viewer, enumJob)
            }
        }

        super.placeButtons(viewer)
    }
}
