package onl.tesseract.srp.service.job.mission

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.format.TextDecoration
import onl.tesseract.lib.logger.LoggerFactory
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.util.Util
import onl.tesseract.lib.util.append
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.domain.job.EnumJob
import onl.tesseract.srp.domain.job.PlayerJobProgression
import onl.tesseract.srp.domain.job.mission.JobMission
import onl.tesseract.srp.repository.hibernate.job.PlayerJobProgressionRepository
import onl.tesseract.srp.repository.hibernate.job.mission.JobMissionRepository
import onl.tesseract.srp.service.item.CustomItemService
import onl.tesseract.srp.service.job.JobService
import onl.tesseract.srp.service.job.PlayerJobService
import onl.tesseract.srp.util.jobsChatFormat
import onl.tesseract.srp.util.jobsChatFormatSuccess
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.slf4j.Logger
import org.springframework.stereotype.Service
import java.util.*
import kotlin.random.Random

private val logger: Logger = LoggerFactory.getLogger(JobMissionService::class.java)

@Service
class JobMissionService(
    private val jobMissionRepository: JobMissionRepository,
    private val jobService: JobService,
    private val playerJobRepo: PlayerJobProgressionRepository,
    private val playerJobService: PlayerJobService,
    private val customItemService: CustomItemService
) {

    private val playerProgress = mutableMapOf<UUID, MutableMap<Long, Int>>()

    fun createRandomMissionForJob(playerId: UUID, enumJob: EnumJob): JobMission {
        val job = jobService.getJob(enumJob)
        val template = job.missionTemplates.random().items.first()

        val rep = getReputation(playerId, enumJob)
        val quantity = Random.nextDouble(template.quantity * 0.9, template.quantity * 1.1) * rep
        val quality = Random.nextDouble(template.minQuality * 0.9, template.minQuality * 1.1) * rep

        val baseStat = job.baseStats[template.material]
            ?: error("No baseStat configured for job $enumJob")

        val reward = quantity * baseStat.moneyGain

        val mission = JobMission(
            id = 0L,
            playerId = playerId,
            job = enumJob,
            material = template.material,
            quantity = quantity.toInt().coerceAtLeast(1),
            minimalQuality = quality.toInt().coerceAtLeast(1),
            reward = reward.toInt()
        )

        val saved = jobMissionRepository.save(mission)
        logger.info("Created mission for player $playerId - Job: $enumJob, Material: ${template.material.name}, Quantity: $quantity, Quality: $quality, Reputation: $rep")
        return saved
    }

    fun getMissionsForPlayer(playerId: UUID): List<JobMission> {
        return jobMissionRepository.findAllByPlayerId(playerId)
    }

    fun getMissionById(id: Long): JobMission? {
        return jobMissionRepository.getById(id)
    }

    private fun getProgress(mission: JobMission): Int {
        return mission.delivered
    }

    fun consumeItemsForMission(player: Player, missionId: Long): Int {
        val currentMission = jobMissionRepository.getById(missionId)
            ?: throw IllegalArgumentException("Mission with ID $missionId does not exist")

        val currentProgress = getProgress(currentMission)
        val toReach = currentMission.quantity - currentProgress

        val removedAmount = customItemService.removeCustomItems(
            player.inventory,
            currentMission.material,
            currentMission.minimalQuality,
            toReach
        )

        if (removedAmount > 0) {
            currentMission.delivered += removedAmount
            jobMissionRepository.save(currentMission)
            logger.info("Player ${player.name} delivered $removedAmount items for mission $missionId")
            player.sendMessage(
                jobsChatFormat + "Tu as déposé " +
                        Component.text("$removedAmount", YELLOW) + " " +
                        Component.text(currentMission.material.displayName, GOLD) + "."
            )

            val updated = jobMissionRepository.getById(currentMission.id)
            if (updated != null) {
                val progressMap = playerProgress.getOrPut(player.uniqueId) { mutableMapOf() }
                progressMap[missionId] = updated.delivered

                if (updated.delivered >= updated.quantity) {
                    completeMission(player, updated)
                } else {
                    val remaining = updated.quantity - updated.delivered
                    player.sendMessage(
                        jobsChatFormat + "Il te reste " +
                                Component.text("$remaining", YELLOW) + " " +
                                Component.text(updated.material.displayName, GOLD) +
                                " à déposer pour compléter la mission."
                    )
                }
            }
        }
        return removedAmount
    }

    fun buildMissionButtonItem(viewer: Player, mission: JobMission, title: String, clickMessage: String): ItemStack {
        val progress = mission.delivered
        val inventoryAmount = viewer.inventory.contents
            .filterNotNull()
            .mapNotNull { runCatching { customItemService.getCustomItemStack(it) }.getOrNull() }
            .filter { it.item.material == mission.material && it.item.quality >= mission.minimalQuality }
            .sumOf { it.amount }

        val total = progress + inventoryAmount
        val gradientColor = Util.getGreenRedGradient(total, mission.quantity)

        return ItemBuilder(mission.material.customMaterial)
            .name(Component.text(title, YELLOW))
            .lore()
            .append(Component.text("Métier : ", GRAY).append(mission.job.name, GOLD))
            .newline()
            .append(Component.text("Qualité minimale : ", GRAY).append("${mission.minimalQuality}%", AQUA))
            .newline()
            .append(Component.text("Récompense : ", GRAY).append("${mission.reward} lys", GREEN))
            .newline()
            .newline()
            .append(Component.text("Quantité déposée : ", GRAY).append("$progress", YELLOW))
            .newline()
            .append(Component.text("Quantité dans l'inventaire : ", GRAY).append("$inventoryAmount", YELLOW))
            .newline()
            .append(Component.text("Total : ", GRAY, TextDecoration.BOLD).append("$total", gradientColor).append(" / ${mission.quantity}", GRAY))
            .newline()
            .append(Component.text(clickMessage, GOLD, TextDecoration.ITALIC))
            .buildLore()
            .build()
    }

    private fun completeMission(player: Player, mission: JobMission) {
        playerJobService.increaseReputation(player.uniqueId, mission.job)
        jobMissionRepository.deleteById(mission.id)

        playerProgress[player.uniqueId]?.remove(mission.id)

        val newRep = getReputation(player.uniqueId, mission.job)
        logger.info("[Mission] New reputation for ${player.name} from job ${mission.job.name} : $newRep")
        player.sendMessage(jobsChatFormatSuccess
                + "✔ Mission complétée ! Tu as gagné "
                + Component.text("${mission.reward} lys ", GOLD)
                + "!"
        )
    }

    private fun getReputation(playerId: UUID, job: EnumJob): Double {
        val progression = playerJobRepo.getById(playerId) ?: PlayerJobProgression(playerId)
        val reputation = progression.reputationByJob.getOrPut(job) {
            playerJobRepo.save(progression)
            1.0
        }
        return reputation
    }

}
