package onl.tesseract.srp.service.job

import jakarta.transaction.Transactional
import onl.tesseract.lib.event.EventService
import onl.tesseract.lib.logger.LoggerFactory
import onl.tesseract.srp.domain.job.EnumJob
import onl.tesseract.srp.domain.job.JobSkill
import onl.tesseract.srp.domain.job.PlayerJobProgression
import onl.tesseract.srp.repository.hibernate.job.PlayerJobProgressionRepository
import org.slf4j.Logger
import org.springframework.stereotype.Service
import java.util.*

val logger: Logger = LoggerFactory.getLogger(PlayerJobService::class.java)

@Service
open class PlayerJobService(
    private val repository: PlayerJobProgressionRepository,
    private val eventService: EventService
) {

    private val lootBatches: MutableMap<UUID, Int> = mutableMapOf()

    open fun getPlayerJobProgression(playerID: UUID): PlayerJobProgression {
        return repository.getById(playerID) ?: PlayerJobProgression(playerID)
    }

    /**
     * Try to unlock a skill and emit a [PlayerJobSkillUnlockedEvent] if successfully unlocked
     * @return True if the skill was unlocked.
     */
    open fun unlockSkill(playerID: UUID, skill: JobSkill): Boolean {
        val progression = getPlayerJobProgression(playerID)
        val added = progression.addSkill(skill)
        if (added) {
            repository.save(progression)
            logger.info("Player $playerID has unlocked skill $skill")
            eventService.callEvent(PlayerJobSkillUnlockedEvent(playerID, skill))
        }
        return added
    }

    /**
     * Register a loot by a player that generated [xpLoot] points of xp. A subsequent call to [processLootBatches] will
     * effectively give the cumulated xp to players.
     */
    open fun registerLoot(playerID: UUID, xpLoot: Int) {
        lootBatches[playerID] = lootBatches.getOrDefault(playerID, 0) + xpLoot
    }

    /**
     * Give all registered xp loot to players and save them.
     */
    @Transactional
    open fun processLootBatches() {
        lootBatches.forEach { (playerID, xp) ->
            addXp(playerID, xp)
        }
        lootBatches.clear()
    }

    fun addXp(playerID: UUID, amount: Int) {
        val progression = getPlayerJobProgression(playerID)
        progression.addXp(amount)
        savePlayerProgression(progression)
    }

    fun addLevel(playerID: UUID, amount: Int) {
        val progression = getPlayerJobProgression(playerID)
        progression.addLevel(amount)
        savePlayerProgression(progression)
    }

    /**
     * Save the player and emit events
     */
    private fun savePlayerProgression(playerJobProgression: PlayerJobProgression) {
        repository.save(playerJobProgression)
        playerJobProgression.consumeEvents {
            logger.info(it.toString())
            eventService.callEvent(it)
        }
    }

    fun clearXp(playerID: UUID) {
        val progression = getPlayerJobProgression(playerID)
        progression.addXp(-progression.xp)
        repository.save(progression)
    }

    fun addSkillPoint(playerID: UUID, points: Int) {
        val progression = getPlayerJobProgression(playerID)
        progression.addSkillPoints(points)
        repository.save(progression)
    }

    @Transactional
    open fun increaseReputation(playerId: UUID, job: EnumJob, amount: Double = 0.005): Double {
        val progression = getPlayerJobProgression(playerId)
        val newValue = progression.reputationByJob.getOrPut(job) { 1.0 } + amount
        progression.reputationByJob[job] = newValue
        repository.save(progression)
        return newValue
    }

}