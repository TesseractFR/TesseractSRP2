package onl.tesseract.srp.domain.player

import onl.tesseract.srp.domain.elytra.EnumElytraUpgrade
import onl.tesseract.srp.domain.exception.NotEnoughMoneyException
import java.util.*

class SrpPlayer(
    val uniqueId: UUID,
    var rank: PlayerRank = PlayerRank.Survivant,
    money: Int = 0,
    var titleID: String = rank.title.id,
    illuminationPoints: Int = 0
) {

    var money: Int = money
        private set
    var illuminationPoints: Int = illuminationPoints
        private set

    fun addMoney(amount: Int): Int {
        if (amount + money < 0)
            throw NotEnoughMoneyException("Money cannot go below 0 (adding $amount to base value $money)")
        money += amount
        return money
    }

    fun addIlluminationPoints(amount: Int): Int {
        if (illuminationPoints + amount < 0)
            throw NotEnoughMoneyException("Illumination points cannot go below 0 (adding $amount to $illuminationPoints)")
        illuminationPoints += amount
        return illuminationPoints
    }

    /**
     * Assign the next rank to the player, and withdraw money.
     * @return False if the player already has the last rank or does not have enough money to buy the rank
     */
    fun buyNextRank(): Boolean {
        val nextRank = rank.next() ?: return false
        if (money < nextRank.cost) return false
        addMoney(-nextRank.cost)
        rank = nextRank
        return true
    }

    fun buyNextElytraUpgrade(price: Int): Boolean {
        if (illuminationPoints < price) return false
        addIlluminationPoints(-price)
        return true
    }

}