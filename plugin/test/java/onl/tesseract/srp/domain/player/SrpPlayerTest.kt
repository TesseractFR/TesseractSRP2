package onl.tesseract.srp.domain.player

import onl.tesseract.srp.domain.exception.NotEnoughMoneyException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class SrpPlayerTest {

    @Test
    fun `addMoney - Should add - When amount is positive`() {
        val player = SrpPlayer(UUID.randomUUID())

        player.addMoney(amount = 10)

        assertEquals(10, player.money)
    }

    @Test
    fun `addMoney - Should remove - When amount is negative and current money is enough`() {
        val player = SrpPlayer(UUID.randomUUID(), money = 20)

        player.addMoney(amount = -10)

        assertEquals(10, player.money)
    }

    @Test
    fun `addMoney - Should throw - When amount is negative and current money is not enough`() {
        val player = SrpPlayer(UUID.randomUUID(), money = 20)

        assertThrows(NotEnoughMoneyException::class.java) {
            player.addMoney(amount = -50)
        }
    }

    @Test
    fun `buyNextRank - Should update rank and money - When player has enough money to buy next rank`() {
        val player = SrpPlayer(UUID.randomUUID(), money = 800, rank = PlayerRank.Survivant)

        val success = player.buyNextRank()

        assertTrue(success)
        assertEquals(PlayerRank.Explorateur, player.rank)
        assertEquals(300, player.money)
    }

    @Test
    fun `buyNextRank - Should not update - When player has the max rank`() {
        val player = SrpPlayer(UUID.randomUUID(), money = 1_000_000_000, rank = PlayerRank.Empereur)

        val success = player.buyNextRank()

        assertFalse(success)
        assertEquals(PlayerRank.Empereur, player.rank)
        assertEquals(1_000_000_000, player.money)
    }

    @Test
    fun `buyNextRank - Should not update - When player does not have enough money to buy next rank`() {
        val player = SrpPlayer(UUID.randomUUID(), money = 10, rank = PlayerRank.Survivant)

        val success = player.buyNextRank()

        assertFalse(success)
        assertEquals(PlayerRank.Survivant, player.rank)
        assertEquals(10, player.money)
    }
}