package onl.tesseract.srp.service.job

import jakarta.transaction.Transactional
import onl.tesseract.lib.event.EventService
import onl.tesseract.lib.logger.LoggerFactory
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

    @Transactional
    open fun addXp(playerID: UUID, amount: Int) {
        val progression = getPlayerJobProgression(playerID)
        progression.addXp(amount)
        repository.save(progression)
    }

    @Transactional
    open fun addLevel(playerID: UUID, amount: Int) {
        val progression = getPlayerJobProgression(playerID)
        progression.addLevel(amount)
        repository.save(progression)
    }

    @Transactional
    open fun clearXp(playerID: UUID) {
        val progression = getPlayerJobProgression(playerID)
        progression.addXp(-progression.xp)
        repository.save(progression)
    }

    @Transactional
    open fun addSkillPoint(playerID: UUID, points: Int) {
        val progression = getPlayerJobProgression(playerID)
        progression.addSkillPoints(points)
        repository.save(progression)
    }
}