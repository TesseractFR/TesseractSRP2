package onl.tesseract.srp.job.adapter.userside.controller.menu;

import net.kyori.adventure.text.Component;
import onl.tesseract.lib.itembuilder.ItemBuilder;
import onl.tesseract.lib.menu.Menu;
import onl.tesseract.lib.menu.MenuSize;
import onl.tesseract.srp.job.domain.model.Job;
import onl.tesseract.srp.job.domain.port.userside.JobCatalogService;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.function.BiConsumer;

public class JobSelectionMenu extends Menu {
    private final JobCatalogService jobCatalogService;
    private final BiConsumer<Player, Job> onJobClick;

    public JobSelectionMenu(String title,
                             JobCatalogService jobCatalogService,
                             BiConsumer<Player, Job> onJobClick) {
        this(title, jobCatalogService, null, onJobClick);
    }

    public JobSelectionMenu(String title,
                             JobCatalogService jobCatalogService,
                             Menu previous,
                             BiConsumer<Player, Job> onJobClick) {
        super(MenuSize.Two, Component.text(title), previous);
        this.jobCatalogService = Objects.requireNonNull(jobCatalogService, "jobCatalogService");
        this.onJobClick = Objects.requireNonNull(onJobClick, "onJobClick");
    }

    @Override
    public void placeButtons(Player viewer) {
        addBackButton();
        addCloseButton();

        int index = 0;
        for (Job job : jobCatalogService.findAllJobs()) {
            addButton(index++, new ItemBuilder(Material.DIAMOND_PICKAXE)
                    .name(job.jobName().value())
                    .build(), event -> onJobClick.accept(viewer, job));
        }

        super.placeButtons(viewer);
    }
}
