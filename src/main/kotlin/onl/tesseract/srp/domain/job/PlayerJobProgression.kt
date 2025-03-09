package onl.tesseract.srp.domain.job

import java.util.*

class PlayerJobProgression(
    val playerID: UUID,
    level: Int = 1,
    xp: Int = 0,
) {
    var level: Int = level
        private set
    var xp: Int = xp
        private set

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
        return passedLevel
    }

    fun addLevel(amount: Int) {
        this.level += amount
        this.level = this.level.coerceAtLeast(1)
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