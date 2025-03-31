package onl.tesseract.srp.service.job.mission

import onl.tesseract.lib.logger.LoggerFactory
import onl.tesseract.srp.domain.job.EnumJob
import onl.tesseract.srp.domain.job.JobHarvestEvent
import onl.tesseract.srp.domain.job.mission.JobMission
import onl.tesseract.srp.exception.PlayerNotConnectedException
import onl.tesseract.srp.repository.hibernate.job.mission.JobMissionRepository
import onl.tesseract.srp.service.item.CustomItemService
import onl.tesseract.srp.service.job.JobService
import onl.tesseract.srp.service.job.PlayerJobService
import org.bukkit.entity.Player
import org.slf4j.Logger
import org.springframework.stereotype.Service
import java.util.*
import kotlin.random.Random

private val logger: Logger = LoggerFactory.getLogger(JobMissionService::class.java)

@Service
class JobMissionService(
    private val jobMissionRepository: JobMissionRepository,
    private val jobService: JobService,
    private val playerJobService: PlayerJobService,
    private val customItemService: CustomItemService
) {
    fun createRandomMissionForJob(playerId: UUID, enumJob: EnumJob): JobMission {
        val job = jobService.getJob(enumJob)
        val template = job.missionTemplates.random().items.first()

        val rep = playerJobService.getPlayerJobProgression(playerId).reputationByJob.getOrDefault(enumJob, 1.0)

        val quantity = Random.nextDouble(template.quantity * 0.9, template.quantity * 1.1) * rep
        val quality = Random.nextDouble(template.minQuality * 0.9, template.minQuality * 1.1) * rep

        val baseStat = job.baseStats[template.material]
            ?: error("No baseStat configured for job $enumJob")

        val playerJobProgression = playerJobService.getPlayerJobProgression(playerId)
        val moneyBonus = playerJobProgression.getMoneyBonus(JobHarvestEvent(playerId, enumJob, template.material))
        val reward = quantity * baseStat.moneyGain * (1 + moneyBonus)

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

    /**
     * @throws IllegalArgumentException If the mission does not exist
     * @throws PlayerNotConnectedException If the player is not connected
     * @throws IllegalStateException If the mission is already completed
     */
    fun consumeItemsForMission(player: Player, missionId: Long): MissionDepositResult {
        if (!player.isConnected) throw PlayerNotConnectedException("Player ${player.name} is not connected")
        val currentMission = jobMissionRepository.getById(missionId)
            ?: throw IllegalArgumentException("Mission with ID $missionId does not exist")
        check(currentMission.delivered < currentMission.quantity) { "Mission $missionId is already completed" }

        val toReach = currentMission.quantity - currentMission.delivered

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


            if (currentMission.delivered >= currentMission.quantity) {
                completeMission(player, currentMission)
                return MissionDepositResult(removedAmount, 0)
            }
        }
        val remaining = currentMission.quantity - currentMission.delivered

        return MissionDepositResult(removedAmount, remaining)
    }

    /**
     * Cancel a mission without giving any reward. Any item delivered will be lost.
     * @return true if the mission was canceled. False if the mission does not exist
     */
    fun cancelMission(missionId: Long): Boolean {
        if (jobMissionRepository.getById(missionId) == null)
            return false
        jobMissionRepository.deleteById(missionId)
        return true
    }

    private fun completeMission(player: Player, mission: JobMission) {
        val newRep = playerJobService.increaseReputation(player.uniqueId, mission.job)
        jobMissionRepository.deleteById(mission.id)

        logger.info("[Mission] New reputation for ${player.name} from job ${mission.job.name} : $newRep")
    }
}

data class MissionDepositResult(val delivered: Int, val remaining: Int)