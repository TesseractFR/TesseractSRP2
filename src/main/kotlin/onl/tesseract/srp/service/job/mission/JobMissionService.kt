package onl.tesseract.srp.service.job.mission

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.*
import onl.tesseract.lib.logger.LoggerFactory
import onl.tesseract.lib.persistantcontainer.NamedspacedKeyProvider
import onl.tesseract.lib.util.append
import org.bukkit.entity.Player
import onl.tesseract.srp.domain.job.EnumJob
import onl.tesseract.srp.domain.job.mission.JobMission
import onl.tesseract.srp.repository.hibernate.job.PlayerJobProgressionRepository
import onl.tesseract.srp.repository.hibernate.job.mission.JobMissionJpaRepository
import onl.tesseract.srp.repository.hibernate.job.mission.JobMissionRepository
import onl.tesseract.srp.repository.hibernate.job.mission.toEntity
import onl.tesseract.srp.service.job.JobService
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.slf4j.Logger
import org.springframework.stereotype.Service
import java.util.*
import kotlin.math.roundToInt

private val logger: Logger = LoggerFactory.getLogger(JobMissionService::class.java)

@Service
class JobMissionService(
    private val namespacedKeyProvider: NamedspacedKeyProvider,
    private val jobMissionRepository: JobMissionRepository,
    private val jobMissionRepositoryJpa: JobMissionJpaRepository,
    private val jobService: JobService,
    private val playerJobRepo: PlayerJobProgressionRepository
) {
    private val playerProgress = mutableMapOf<UUID, MutableMap<Long, Int>>()

    fun createRandomMissionForJob(playerId: UUID, job: EnumJob): JobMission {
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
            id = null,
            playerId = playerId,
            job = job,
            material = material,
            quantity = quantity,
            minimalQuality = quality,
            reward = reward
        )

        val entity = jobMissionRepositoryJpa.save(mission.toEntity())
        logger.info("Created mission for player $playerId - Job: $job, Material: ${material.name}, Quantity: $quantity, Quality: $quality, Reputation: $rep")
        return entity.toDomain()
    }

    fun getMissionsForPlayer(playerId: UUID): List<JobMission> {
        return jobMissionRepository.findAllByPlayerId(playerId)
    }

    fun getProgress(player: Player, mission: JobMission): Int {
        return mission.id?.let {
            playerProgress[player.uniqueId]?.get(it) ?: 0
        } ?: 0
    }

    fun consumeItemsForMission(player: Player, mission: JobMission): Int {
        val missionId = mission.id
            ?: throw IllegalArgumentException("Mission has no ID and cannot be processed")
        val currentMission = jobMissionRepository.getById(missionId)
            ?: throw IllegalArgumentException("Mission with ID $missionId does not exist")

        val material = currentMission.material.customMaterial
        val inventory = player.inventory
        val currentProgress = getProgress(player, currentMission)
        val toReach = currentMission.quantity - currentProgress
        var remainingToRemove = toReach
        var removedAmount = 0

        val toRemoveMap = mutableMapOf<Int, Int>()

        inventory.contents.forEachIndexed { index, item ->
            if (remainingToRemove <= 0) return@forEachIndexed

            if (item != null && item.type == material && getQuality(item) >= currentMission.minimalQuality) {
                val amount = item.amount
                val toRemove = minOf(amount, remainingToRemove)
                if (toRemove > 0) {
                    toRemoveMap[index] = toRemove
                    removedAmount += toRemove
                    remainingToRemove -= toRemove
                }
            }
        }
        toRemoveMap.forEach { (index, amountToRemove) ->
            val item = inventory.getItem(index) ?: return@forEach
            if (item.amount <= amountToRemove) {
                inventory.setItem(index, null)
            } else {
                item.amount -= amountToRemove
            }
        }
        if (removedAmount > 0) {
            val progressMap = playerProgress.getOrPut(player.uniqueId) { mutableMapOf() }
            progressMap[missionId] = (progressMap[missionId] ?: 0) + removedAmount
            logger.info("Player ${player.name} delivered $removedAmount items for mission $missionId")
        }
        return removedAmount
    }

    fun getMissionProgressComponent(player: Player, mission: JobMission): Component {
        val progress = getProgress(player, mission)
        val inventoryAmount = player.inventory.contents
            .filterNotNull()
            .filter { it.type == mission.material.customMaterial }
            .filter { getQuality(it) >= mission.minimalQuality }
            .sumOf { it.amount }

        val total = progress + inventoryAmount
        val color = if (total >= mission.quantity) GREEN else RED

        return Component.text("Quantité déposée : ", GRAY)
            .append("$progress", YELLOW)
            .append(" + ", GRAY)
            .append("$inventoryAmount", color)
            .append(" / ", GRAY)
            .append("${mission.quantity}", GRAY)
    }

    fun deleteMission(player: Player, mission: JobMission) {
        val missionId = mission.id
            ?: throw IllegalArgumentException("Cannot delete a mission without ID.")
        jobMissionRepository.deleteById(missionId)
        increaseReputation(player.uniqueId, mission.job)
        val newRep = getReputation(player.uniqueId, mission.job)
        logger.info("[Mission] New reputation for ${player.name} from job ${mission.job.name} : $newRep")
    }

    fun getQuality(item: ItemStack): Int {
        val meta = item.itemMeta ?: return 0
        val container = meta.persistentDataContainer
        val key = namespacedKeyProvider.get("quality")
        return container.get(key, PersistentDataType.INTEGER) ?: 0
    }

    private fun getReputation(playerId: UUID, job: EnumJob): Double {
        val progression = playerJobRepo.getById(playerId)
            ?: throw IllegalArgumentException("Player progression not found for $playerId")
        return progression.reputationByJob[job] ?: 1.0
    }

    private fun increaseReputation(playerId: UUID, job: EnumJob, amount: Double = 0.005) {
        val progression = playerJobRepo.getById(playerId)
            ?: throw IllegalArgumentException("Cannot increase reputation, player progression not found for $playerId")
        val current = progression.reputationByJob.getOrDefault(job, 1.0)
        progression.reputationByJob[job] = current + amount
        playerJobRepo.save(progression)
    }

}
