package onl.tesseract.srp.domain.job

import onl.tesseract.srp.service.job.PlayerLevelUpEvent
import org.bukkit.event.Event
import java.util.*

class PlayerJobProgression(
    val playerID: UUID,
    level: Int = 1,
    xp: Int = 0,
    skillPoints: Int = 0,
    skills: List<JobSkill> = listOf()
) {
    var level: Int = level
        private set
    var xp: Int = xp
        private set
    var skillPoints: Int = skillPoints
        private set
    private val _skills: MutableList<JobSkill> = skills.toMutableList()
    val skills: List<JobSkill>
        get() = _skills

    private val _events: MutableList<Event> = mutableListOf()

    fun consumeEvents(consume: (Event) -> Unit) {
        val iterator = _events.iterator()
        while (iterator.hasNext()) {
            consume(iterator.next())
            iterator.remove()
        }
    }

    /**
     * Give (or remove) XP to the player. If amount is positive and enough to increase the level, the level is updated,
     * and the xp decreased. Removing more than the current XP amount will set XP to 0 without decreasing the level.
     * @return Amount of passed levels
     */
    fun addXp(amount: Int): Int {
        this.xp += amount
        var passedLevel = 0
        while (this.xp >= getXpForLevel(this.level + 1)) {
            this.xp -= getXpForLevel(this.level + 1)
            this.level++
            passedLevel++
        }
        this.xp = this.xp.coerceAtLeast(0)
        this.skillPoints += passedLevel
        if (passedLevel > 0) _events.add(PlayerLevelUpEvent(playerID, this.level, passedLevel))
        return passedLevel
    }

    fun addLevel(amount: Int) {
        this.level += amount
        this.level = this.level.coerceAtLeast(1)
        if (amount > 0) _events.add(PlayerLevelUpEvent(playerID, this.level, amount))
    }

    fun addSkillPoints(points: Int) {
        this.skillPoints += points
        this.skillPoints = this.skillPoints.coerceAtLeast(0)
    }

    fun addSkill(skill: JobSkill): Boolean {
        if (skill.cost > skillPoints || !isSkillAvailable(skill)) return false
        if (hasSkill(skill)) return false
        _skills.add(skill)
        skillPoints -= skill.cost
        return true
    }

    fun hasSkill(skill: JobSkill): Boolean = _skills.contains(skill)

    fun isSkillAvailable(skill: JobSkill): Boolean {
        return skill.parent == null || isSkillAvailable(skill.parent)
    }

    fun getLootChanceBonus(event: JobHarvestEvent): Float {
        return this.skills.sumOf { it.bonus.getLootChanceBonus(event).toDouble() }.toFloat()
    }

    fun getMoneyBonus(event: JobHarvestEvent): Float {
        return this.skills.sumOf { it.bonus.getMoneyBonus(event).toDouble() }.toFloat()
    }

    fun getQualityBonus(event: JobHarvestEvent): Int {
        return this.skills.sumOf { it.bonus.getQualityBonus(event) }
    }

    private fun getXpForLevel(level: Int): Int {
        return 100 * (level - 1)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayerJobProgression

        if (xp != other.xp) return false
        if (level != other.level) return false

        return true
    }

    override fun hashCode(): Int {
        var result = xp
        result = 31 * result + level
        return result
    }

    override fun toString(): String {
        return "PlayerJobProgression(xp=$xp, level=$level)"
    }
}