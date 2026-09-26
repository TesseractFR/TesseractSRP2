package onl.tesseract.srp.job.adapter.userside.controller.listener;

import onl.tesseract.srp.job.domain.model.Material;
import onl.tesseract.srp.job.domain.model.PlayerID;
import onl.tesseract.srp.job.domain.model.source.Source;
import onl.tesseract.srp.job.domain.model.source.SourceType;
import onl.tesseract.srp.job.domain.port.userside.JobService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.springframework.stereotype.Component;

@Component
public class BlockBreakListener implements Listener {

    private final JobService jobService;

    public BlockBreakListener(JobService jobService) {
        this.jobService = jobService;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        jobService.process(new PlayerID(event.getPlayer().getUniqueId()),
                new Source(new Material(event.getBlock().getType().name()), SourceType.BLOCK));
    }

}
