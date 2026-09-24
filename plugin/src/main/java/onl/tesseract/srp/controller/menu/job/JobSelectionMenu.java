package onl.tesseract.srp.controller.menu.job;

import net.kyori.adventure.text.Component;
import onl.tesseract.lib.menu.ItemBuilder;
import onl.tesseract.lib.menu.Menu;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.job.domain.model.Job;
import onl.tesseract.srp.job.domain.port.userside.JobService;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.function.BiConsumer;

public class JobSelectionMenu extends Menu {
    private final JobService jobService;
    private final BiConsumer<Player, Job> onJobClick;

    public JobSelectionMenu(String title,
                             JobService jobService,
                             BiConsumer<Player, Job> onJobClick) {
        this(title, jobService, null, onJobClick);
    }

    public JobSelectionMenu(String title,
                             JobService jobService,
                             Menu previous,
                             BiConsumer<Player, Job> onJobClick) {
        super(MenuSize.Two, Component.text(title), previous);
        this.jobService = Objects.requireNonNull(jobService, "jobService");
        this.onJobClick = Objects.requireNonNull(onJobClick, "onJobClick");
    }

    @Override
    public void placeButtons(Player viewer) {
        addBackButton();
        addCloseButton();

        int index = 0;
        for (Job job : jobService.listJobs()) {
            addButton(index++, new ItemBuilder(Material.DIAMOND_PICKAXE)
                    .name(job.jobName().value())
                    .build(), event -> onJobClick.accept(viewer, job));
        }

        super.placeButtons(viewer);
    }
}
