package onl.tesseract.srp.controller.menu.job

import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.util.toComponent
import onl.tesseract.srp.job.domain.model.Job
import onl.tesseract.srp.job.domain.port.userside.JobService
import org.bukkit.Material
import org.bukkit.entity.Player

class JobSelectionMenu(
    title: String,
    val jobService: JobService,
    previous: Menu? = null,
    private val onJobClick: (viewer: Player, job: Job) -> Unit
) : Menu(MenuSize.Two, title.toComponent(), previous) {

    override fun placeButtons(viewer: Player) {
        addBackButton()
        addCloseButton()

        jobService.listJobs().forEachIndexed { index, job ->
            addButton(index, ItemBuilder(Material.DIAMOND_PICKAXE)
                    .name(job.jobName().value())
                    .build()) {
                onJobClick(viewer, job)
            }

        }

        super.placeButtons(viewer)
    }
}
