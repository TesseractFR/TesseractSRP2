package onl.tesseract.srp.domain.job

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

class PlayerJobProgressionTest {

    @Test
    fun `addXp - Should not increase level - When adding little xp`() {
        val info = playerJobProgression()

        val passedLevel = info.addXp(20)

        assertEquals(0, passedLevel)
        assertEquals(playerJobProgression(1, 20), info)
    }

    @Test
    fun `addXp - Should increase level from 1 to 2 - When adding 100 xp`() {
        val info = playerJobProgression()

        val passedLevel = info.addXp(100)

        assertEquals(playerJobProgression(2, 0), info)
        assertEquals(1, passedLevel)
    }

    @Test
    fun `addXp - Should increase level from 1 to 2 with excess xp - When adding 120 xp`() {
        val info = playerJobProgression()

        val passedLevel = info.addXp(120)

        assertEquals(1, passedLevel)
        assertEquals(playerJobProgression(2, 20), info)
    }

    @Test
    fun `addXp - Should increase level from 1 to 3 - When adding 300 xp`() {
        val info = playerJobProgression()

        val passedLevel = info.addXp(300)

        assertEquals(2, passedLevel)
        assertEquals(playerJobProgression(3, 0), info)
    }

    @Test
    fun `addXp - Should not go below 0 - When removing XP`() {
        val info = playerJobProgression(1, 20)

        val passedLevel = info.addXp(-50)

        assertEquals(0, passedLevel)
        assertEquals(playerJobProgression(1, 0), info)
    }
}

fun playerJobProgression(level: Int = 1, xp: Int = 0) = PlayerJobProgression(UUID.randomUUID(), level, xp)