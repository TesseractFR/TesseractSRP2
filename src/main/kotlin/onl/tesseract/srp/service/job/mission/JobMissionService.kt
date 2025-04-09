package onl.tesseract.srp.service.job.mission

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.*
import onl.tesseract.lib.logger.LoggerFactory
import onl.tesseract.lib.util.Util
import onl.tesseract.lib.util.append
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.controller.menu.job.mission.JobMissionSelectionMenu
import org.bukkit.entity.Player
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
import org.slf4j.Logger
import org.springframework.stereotype.Service
import java.util.*
import kotlin.math.roundToInt

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

    fun createRandomMissionForJob(playerId: UUID, job: EnumJob): Boolean {
        val possibleMaterials = jobService.getCustomMaterialsForJob(job)
        val material = possibleMaterials.random()

        val baseQuantity = 2..4
        val baseQuality = 10..15

        val rep = getReputation(playerId, job)

        val quantityRange = (baseQuantity.first * rep).roundToInt()..(baseQuantity.last * rep).roundToInt()
        val qualityRange = (baseQuality.first * rep).roundToInt()..(baseQuality.last * rep).roundToInt()

        val quantity = quantityRange.random().coerceAtLeast(1)
        val quality = qualityRange.random().coerceAtLeast(1)
        val reward = quantity * quality

        val mission = JobMission(
            id = 0L,
            playerId = playerId,
            job = job,
            material = material,
            quantity = quantity,
            minimalQuality = quality,
            reward = reward
        )
        return try {
            jobMissionRepository.save(mission)
            logger.info("Created mission for player $playerId - Job: $job, Material: ${material.name}, Quantity: $quantity, Quality: $quality, Reputation: $rep")
            true
        } catch (e: Exception) {
            logger.error("Failed to create mission for $playerId - Job: $job", e)
            false
        }
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

    fun getMissionProgressComponent(player: Player, mission: JobMission): Component {
        val progress = mission.delivered
        val inventoryAmount = player.inventory.contents
            .filterNotNull()
            .mapNotNull { runCatching { customItemService.getCustomItemStack(it) }.getOrNull() }
            .filter { it.item.material == mission.material && it.item.quality >= mission.minimalQuality }
            .sumOf { it.amount }

        val total = progress + inventoryAmount
        val gradientColor = Util.getGreenRedGradient(total, mission.quantity)

        return Component.text("Quantité déposée : ", GRAY)
            .append("$progress", YELLOW)
            .append(" + ", GRAY)
            .append("$inventoryAmount", gradientColor)
            .append(" / ", GRAY)
            .append("${mission.quantity}", GRAY)
    }

    private fun completeMission(player: Player, mission: JobMission) {
        playerJobService.increaseReputation(player.uniqueId, mission.job)
        jobMissionRepository.deleteById(mission.id)

        playerProgress[player.uniqueId]?.remove(mission.id)
        JobMissionSelectionMenu.playerMissions[player.uniqueId]?.values?.removeIf { it.id == mission.id }

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
