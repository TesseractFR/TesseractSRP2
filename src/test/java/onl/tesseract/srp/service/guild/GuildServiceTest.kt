package onl.tesseract.srp.service.guild

import onl.tesseract.lib.event.EventService
import onl.tesseract.srp.domain.player.SrpPlayer
import onl.tesseract.srp.domain.world.SrpWorld
import onl.tesseract.srp.service.money.MoneyLedgerService
import onl.tesseract.srp.service.player.SrpPlayerService
import onl.tesseract.srp.testutils.GuildInMemoryRepository
import onl.tesseract.srp.testutils.SrpPlayerInMemoryRepository
import onl.tesseract.srp.testutils.mockWorld
import org.bukkit.Location
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.mockito.Mockito.mock
import java.util.*

class GuildServiceTest {

    private val playerRepository = SrpPlayerInMemoryRepository()
    private val guildRepository = GuildInMemoryRepository()

    private lateinit var guildService: GuildService

    @BeforeEach
    fun setup() {
        val playerService = SrpPlayerService(
            playerRepository,
            mock(MoneyLedgerService::class.java),
            mock(EventService::class.java)
        )
        guildService = GuildService(guildRepository, playerService)
    }

    @Test
    fun `Create guild - Should return the new guild - When player does not have a guild and has enough money`() {
        // Given
        val player = player(money = 10_000)
        val location = Location(mockWorld(SrpWorld.GuildWorld.bukkitName), 500.0, 0.0, 500.0)

        // When
        val result = guildService.createGuild(player.uniqueId, location, "MyGuild")

        // Then
        assertNotNull(result.guild)
        assertEquals("MyGuild", result.guild!!.name)
        assertEquals(player.uniqueId, result.guild!!.leaderId)
        assertEquals(result.guild, guildRepository.getById(result.guild!!.id))
        assertEquals(0, player.money)
    }

    @Test
    fun `Create guild - Should return failure - When player does not have enough money`() {
        // Given
        val player = player(money = 5_000)
        val location = Location(mockWorld(SrpWorld.GuildWorld.bukkitName), 500.0, 0.0, 500.0)

        // When
        val result = guildService.createGuild(player.uniqueId, location, "MyGuild")

        // Then
        assertNull(result.guild)
        assertEquals(GuildCreationResult.Reason.NotEnoughMoney, result.reason)
        assertEquals(5_000, player.money)
    }

    @Test
    fun `Create guild - Should return failure - When location is not in guild world`() {
        // Given
        val player = player(money = 10_000)
        val world = mockWorld("hey")
        val location = Location(world, 500.0, 0.0, 500.0)

        // When
        val result = guildService.createGuild(player.uniqueId, location, "MyGuild")

        // Then
        assertNull(result.guild)
        assertEquals(GuildCreationResult.Reason.InvalidWorld, result.reason)
    }

    @Test
    fun `Create guild - Should return failure - When location is near spawn`() {
        // Given
        val player = player(money = 10_000)
        val world = mockWorld(SrpWorld.GuildWorld.bukkitName)
        val location = Location(world, 50.0, 0.0, 50.0)

        // When
        val result = guildService.createGuild(player.uniqueId, location, "MyGuild")

        // Then
        assertNull(result.guild)
        assertEquals(GuildCreationResult.Reason.NearSpawn, result.reason)
    }

    @Test
    fun `Create guild - Should claim 9 chunks - When creating a new guild`() {
        // Given
        val player = player(money = 10_000)
        val world = mockWorld(SrpWorld.GuildWorld.bukkitName)
        val location = Location(world, 500.0, 0.0, 500.0)

        // When
        val result = guildService.createGuild(player.uniqueId, location, "MyGuild")

        // Then
        assertNotNull(result.guild)
        assertEquals(9, result.guild!!.chunks.size)
    }

    private fun player(money: Int = 0): SrpPlayer {
        val srpPlayer = SrpPlayer(UUID.randomUUID(), money = money)
        return playerRepository.save(srpPlayer)
    }
}