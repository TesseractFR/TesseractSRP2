package onl.tesseract.srp.service.job

import jakarta.transaction.Transactional
import onl.tesseract.srp.domain.job.PlayerJobProgression
import onl.tesseract.srp.repository.hibernate.job.PlayerJobProgressionRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
open class PlayerJobService(private val repository: PlayerJobProgressionRepository) {

    open fun getPlayerJobProgression(playerID: UUID): PlayerJobProgression {
        return repository.getById(playerID) ?: PlayerJobProgression(playerID)
    }

    @Transactional
    open fun addXp(playerID: UUID, amount: Int) {
        val progression = getPlayerJobProgression(playerID)
        progression.addXp(amount)
        // TODO level-up event
        repository.save(progression)
    }

    @Transactional
    open fun addLevel(playerID: UUID, amount: Int) {
        val progression = getPlayerJobProgression(playerID)
        progression.addLevel(amount)
        // TODO level-up event
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