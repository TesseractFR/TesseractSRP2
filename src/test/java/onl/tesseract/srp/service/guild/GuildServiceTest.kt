package onl.tesseract.srp.service.guild

import onl.tesseract.lib.event.EventService
import onl.tesseract.srp.domain.guild.Guild
import onl.tesseract.srp.domain.player.PlayerRank
import onl.tesseract.srp.domain.player.SrpPlayer
import onl.tesseract.srp.domain.world.SrpWorld
import onl.tesseract.srp.repository.hibernate.MoneyLedgerRepository
import onl.tesseract.srp.service.money.MoneyLedgerService
import onl.tesseract.srp.service.money.TransferService
import onl.tesseract.srp.service.player.SrpPlayerService
import onl.tesseract.srp.testutils.GuildInMemoryRepository
import onl.tesseract.srp.testutils.SrpPlayerInMemoryRepository
import onl.tesseract.srp.testutils.fixture.SrpPlayerDomainTest
import onl.tesseract.srp.testutils.mockWorld
import org.bukkit.Location
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.mockito.Mockito.mock
import java.util.*
import kotlin.random.Random

class GuildServiceTest : SrpPlayerDomainTest {

    override val playerRepository = SrpPlayerInMemoryRepository()
    private val guildRepository = GuildInMemoryRepository()

    private lateinit var guildService: GuildService

    @BeforeEach
    fun setup() {
        val ledgerService = MoneyLedgerService(mock(MoneyLedgerRepository::class.java))
        val playerService = SrpPlayerService(
            playerRepository,
            ledgerService,
            mock(EventService::class.java)
        )
        guildService = GuildService(guildRepository, playerService, ledgerService, TransferService(ledgerService))
    }

    @Test
    fun `Create guild - Should return the new guild - When player does not have a guild and has enough money`() {
        // Given
        val player = player(money = 10_000)
        val location = Location(mockWorld(SrpWorld.GuildWorld.bukkitName), 500.0, 0.0, 500.0)

        // When
        val result = guildService.createGuild(player.uniqueId, location, "MyGuild")

        // Then
        assertTrue(result.isSuccess())
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
        assertTrue(GuildCreationResult.Reason.NotEnoughMoney in result.reason)
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
        assertTrue(GuildCreationResult.Reason.InvalidWorld in result.reason)
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
        assertTrue(GuildCreationResult.Reason.NearSpawn in result.reason)
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
        assertTrue(result.isSuccess())
        assertNotNull(result.guild)
        assertEquals(9, result.guild!!.cityChunks.size)
    }

    @Test
    fun `Create guild - Should return failure - When creating a new guild in another guild's territory`() {
        // Given
        val player1 = player(money = 10_000)
        val player2 = player(money = 10_000)
        val world = mockWorld(SrpWorld.GuildWorld.bukkitName)
        val location1 = Location(world, 500.0, 0.0, 500.0)
        val location2 = Location(world, 480.0, 0.0, 480.0)

        // When
        val result1 = guildService.createGuild(player1.uniqueId, location1, "MyGuild")
        val result2 = guildService.createGuild(player2.uniqueId, location2, "OtherGuild")

        // Then
        assertTrue(result1.isSuccess())
        assertTrue(GuildCreationResult.Reason.NearGuild in result2.reason)
    }

    @Test
    fun `Create guild - Should return failure - When name taken`() {
        // Given
        val player = player(money = 10_000)
        val world = mockWorld(SrpWorld.GuildWorld.bukkitName)
        val location1 = Location(world, 200.0, 0.0, 200.0)
        guildRepository.save(Guild(id = 1, leaderId = UUID.randomUUID(), "MyGuild", location1))
        val location2 = Location(world, 480.0, 0.0, 480.0)

        // When
        val result = guildService.createGuild(player.uniqueId, location2, "MyGuild")

        // Then
        assertTrue(GuildCreationResult.Reason.NameTaken in result.reason)
    }

    @Test
    fun `Create guild - Should return failure - When player already has a guild`() {
        // Given
        val player = player(money = 10_000)
        val world = mockWorld(SrpWorld.GuildWorld.bukkitName)
        val location1 = Location(world, 500.0, 0.0, 500.0)
        val location2 = Location(world, 200.0, 0.0, 200.0)
        guildRepository.save(Guild(1, leaderId = player.uniqueId, "hello", location1))

        // When
        val result = guildService.createGuild(player.uniqueId, location2, "MyGuild")

        // Then
        assertTrue(GuildCreationResult.Reason.PlayerHasGuild in result.reason)
    }

    @Test
    fun `Create guild - Should return failure - When player does not have rank Baron`() {
        // Given
        val player = player(money = 10_000, rank = PlayerRank.Survivant)
        val world = mockWorld(SrpWorld.GuildWorld.bukkitName)
        val location1 = Location(world, 500.0, 0.0, 500.0)
        val location2 = Location(world, 200.0, 0.0, 200.0)
        guildRepository.save(Guild(1, leaderId = player.uniqueId, "hello", location1))

        // When
        val result = guildService.createGuild(player.uniqueId, location2, "MyGuild")

        // Then
        assertTrue(GuildCreationResult.Reason.Rank in result.reason)
    }

    @Test
    fun `Add member - Should add member - When inviting then joining`() {
        // Given
        val bob = player()
        val alice = player()
        val aliceGuild = guild(leader = alice)

        // When / Then
        val invitationResult = guildService.invite(aliceGuild.id, bob.uniqueId)
        assertEquals(InvitationResult.Invited, invitationResult)
        val joinResult = guildService.join(aliceGuild.id, bob.uniqueId)
        assertEquals(JoinResult.Joined, joinResult)
    }

    @Test
    fun `Add member - Should add member - When asking to join then inviting`() {
        // Given
        val bob = player()
        val alice = player()
        val aliceGuild = guild(leader = alice)

        // When / Then
        val joinResult = guildService.join(aliceGuild.id, bob.uniqueId)
        assertEquals(JoinResult.Requested, joinResult)
        val invitationResult = guildService.invite(aliceGuild.id, bob.uniqueId)
        assertEquals(InvitationResult.Joined, invitationResult)
    }

    @Test
    fun `Add member - Should fail - When inviting a player who already has a guild`() {
        // Given
        val bob = player()
        val alice = player()
        guild(leader = bob)
        val aliceGuild = guild(leader = alice)

        // When
        val invitationResult = guildService.invite(aliceGuild.id, bob.uniqueId)

        // Then
        assertEquals(InvitationResult.Failed, invitationResult)
    }

    @Test
    fun `Add member - Should fail - When asking to join when already in a guild`() {
        // Given
        val bob = player()
        val alice = player()
        guild(leader = bob)
        val aliceGuild = guild(leader = alice)

        // When
        val joinResult = guildService.join(aliceGuild.id, bob.uniqueId)

        // Then
        assertEquals(JoinResult.Failed, joinResult)
    }

    @Test
    fun `Add member - Should clear invitation - When joined`() {
        // Given
        val bob = player()
        val alice = player()
        val aliceGuild = guild(leader = alice)

        // When
        val invitationResult = guildService.invite(aliceGuild.id, bob.uniqueId)
        val joinResult = guildService.join(aliceGuild.id, bob.uniqueId)

        // Then
        assertEquals(InvitationResult.Invited, invitationResult)
        assertEquals(JoinResult.Joined, joinResult)
        assertFalse(guildRepository.getById(aliceGuild.id)!!.invitations.contains(bob.uniqueId))
    }

    @Test
    fun `Deposit money - Should transfer from player to guild - When player has enough money`() {
        // Given
        val player = player(money = 150)
        val guild = guild(leader = player)

        // When
        val success = guildService.depositMoney(guild.id, player.uniqueId, 100u)

        // Then
        assertTrue(success)
        assertEquals(50, playerRepository.getById(player.uniqueId)!!.money)
        assertEquals(100, guild.money)
    }

    @Test
    fun `Deposit money - Should return false - When player does not have enough money`() {
        // Given
        val player = player(money = 50)
        val guild = guild(leader = player)

        // When
        val success = guildService.depositMoney(guild.id, player.uniqueId, 100u)

        // Then
        assertFalse(success)
        assertEquals(50, playerRepository.getById(player.uniqueId)!!.money)
        assertEquals(0, guild.money)
    }

    @Test
    fun `Deposit money - Should fail - When player is not in guild`() {
        // Given
        val player = player(money = 150)
        val bob = player()
        val guild = guild(leader = bob)

        // When / Then
        assertThrows(IllegalArgumentException::class.java) {
            guildService.depositMoney(guild.id, player.uniqueId, 100u)
        }
    }

    private fun guild(leader: SrpPlayer): Guild {
        val guild = Guild(
            id = Random.nextInt(),
            leaderId = leader.uniqueId,
            name = "MyGuild",
            spawnLocation = mock(Location::class.java)
        )
        return guildRepository.save(guild)
    }
}
