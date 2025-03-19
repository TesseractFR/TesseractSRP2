package onl.tesseract.srp.controller.event.job

import onl.tesseract.lib.logger.LoggerFactory
import onl.tesseract.srp.service.job.JobLootItemEvent
import onl.tesseract.srp.service.job.PlayerJobService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.slf4j.Logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

val logger: Logger = LoggerFactory.getLogger(CustomItemHarvestListener::class.java)

@Component
class CustomItemHarvestListener(private val playerJobService: PlayerJobService) : Listener {

    @EventHandler
    fun onCustomItemLoot(event: JobLootItemEvent) {
        playerJobService.registerLoot(event.playerID, event.xp)
    }

    @Scheduled(fixedRate = 15, timeUnit = TimeUnit.SECONDS, scheduler = "bukkitScheduler")
    fun processBatchesSchedule() {
        logger.trace("Processing items harvest batches")
        playerJobService.processLootBatches()
    }
}