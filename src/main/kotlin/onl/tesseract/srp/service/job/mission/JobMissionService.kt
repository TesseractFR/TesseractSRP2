package onl.tesseract.srp.service.job.mission

import onl.tesseract.lib.persistantcontainer.NamedspacedKeyProvider
import org.bukkit.entity.Player
import onl.tesseract.srp.domain.item.CustomMaterial
import onl.tesseract.srp.domain.job.EnumJob
import onl.tesseract.srp.domain.job.mission.JobMission
import onl.tesseract.srp.repository.hibernate.job.mission.JobMissionJpaRepository
import onl.tesseract.srp.repository.hibernate.job.mission.JobMissionRepository
import onl.tesseract.srp.repository.hibernate.job.mission.toEntity
import onl.tesseract.srp.service.job.JobService
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.springframework.stereotype.Service
import java.util.*

@Service
class JobMissionService(
    private val namespacedKeyProvider: NamedspacedKeyProvider,
    private val jobMissionRepository: JobMissionRepository,
    private val jobMissionRepositoryJpa: JobMissionJpaRepository,
    private val jobService: JobService
) {

    private val playerProgress = mutableMapOf<UUID, MutableMap<Long, Int>>()


    fun createRandomMissionForJob(playerId: UUID, job: EnumJob): JobMission {
        val possibleMaterials = jobService.getCustomMaterialsForJob(job)
        val material = possibleMaterials.random()
        val quantity = (1..5).random()
        val quality = (10..20).random()
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

        // Retirer les items de l'inventaire
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
        }

        return removedAmount
    }


    fun deleteMission(mission: JobMission) {
        mission.id?.let { jobMissionRepository.deleteById(it) }
    }

    private fun getQuality(item: ItemStack): Int {
        val meta = item.itemMeta ?: return 0
        val container = meta.persistentDataContainer
        val key = namespacedKeyProvider.get("quality")

        return container.get(key, PersistentDataType.INTEGER) ?: 0
    }

}
