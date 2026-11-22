package onl.tesseract.srp.job.adapter.userside.controller.listener;

import onl.tesseract.srp.job.domain.model.Material;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.job.domain.model.source.Source;
import onl.tesseract.srp.job.domain.model.source.SourceType;
import onl.tesseract.srp.job.domain.port.userside.JobHarvestService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.springframework.stereotype.Component;

@Component
public class BlockBreakListener implements Listener {

    private final JobHarvestService jobHarvestService;

    public BlockBreakListener(JobHarvestService jobHarvestService) {
        this.jobHarvestService = jobHarvestService;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        jobHarvestService.process(new PlayerId(event.getPlayer().getUniqueId()),
                new Source(new Material(event.getBlock().getType().name()), SourceType.BLOCK));
    }

}
