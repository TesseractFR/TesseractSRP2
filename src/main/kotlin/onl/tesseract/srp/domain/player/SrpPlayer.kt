package onl.tesseract.srp.domain.player

import onl.tesseract.srp.domain.exception.NotEnoughMoneyException
import java.util.*

class SrpPlayer(
    val uniqueId: UUID,
    var rank: PlayerRank = PlayerRank.Survivant,
    money: Int = 0,
) {

    var money: Int = money
        private set

    fun addMoney(amount: Int): Int {
        if (amount + money < 0)
            throw NotEnoughMoneyException("Money cannot go below 0 (adding $amount to base value $money)")
        money += amount
        return money
    }
}