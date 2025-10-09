package onl.tesseract.srp.service.guild

import onl.tesseract.lib.chat.ChatEntryService
import onl.tesseract.lib.event.EventService
import onl.tesseract.srp.domain.guild.Guild
import onl.tesseract.srp.domain.guild.GuildChunk
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
import org.mockito.Mockito.`when`
import java.util.*
import kotlin.random.Random

class GuildServiceTest : SrpPlayerDomainTest {

    override val playerRepository = SrpPlayerInMemoryRepository()
    private val guildRepository = GuildInMemoryRepository()

    private lateinit var guildService: GuildService

    @BeforeEach
    fun setup() {
        val eventService = mock(EventService::class.java)
        val ledgerService = MoneyLedgerService(mock(MoneyLedgerRepository::class.java))
        val playerService = SrpPlayerService(
            playerRepository,
            ledgerService,
            mock(EventService::class.java)
        )
        val chatEntryService = mock(ChatEntryService::class.java)
        guildService = GuildService(
            guildRepository, playerService, eventService, ledgerService,
            TransferService(ledgerService), chatEntryService
        )
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
        assertEquals(9, result.guild!!.chunks.size)
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
        val location = Location(world, 500.0, 0.0, 500.0)

        // When
        val result = guildService.createGuild(player.uniqueId, location, "MyGuild")

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
    fun `Kick member - Should kick member - When leader`() {
        // Given
        val bob = player()
        val alice = player()
        val aliceGuild = guild(leader = alice)
        assertEquals(InvitationResult.Invited, guildService.invite(aliceGuild.id, bob.uniqueId))
        assertEquals(JoinResult.Joined, guildService.join(aliceGuild.id, bob.uniqueId))

        // When
        val kickResult = guildService.kickMember(aliceGuild.id, alice.uniqueId, bob.uniqueId)

        // Then
        assertEquals(KickResult.Success, kickResult)
        val updated = guildRepository.getById(aliceGuild.id)!!
        assertFalse(updated.members.any { it.playerID == bob.uniqueId })
        assertNull(guildRepository.findGuildByMember(bob.uniqueId))
    }

    @Test
    fun `Kick member - Should not  kick member - When not leader`() {
        // Given
        val bob = player()
        val alice = player()
        val michel = player()
        val aliceGuild = guild(leader = alice)
        assertEquals(InvitationResult.Invited, guildService.invite(aliceGuild.id, bob.uniqueId))
        assertEquals(JoinResult.Joined, guildService.join(aliceGuild.id, bob.uniqueId))
        assertEquals(InvitationResult.Invited, guildService.invite(aliceGuild.id, michel.uniqueId))
        assertEquals(JoinResult.Joined, guildService.join(aliceGuild.id, michel.uniqueId))

        // When
        val kickResult = guildService.kickMember(aliceGuild.id, michel.uniqueId, bob.uniqueId)

        // Then
        assertEquals(KickResult.NotAuthorized, kickResult)
        val updated = guildRepository.getById(aliceGuild.id)!!
        assertTrue(updated.members.any { it.playerID == bob.uniqueId })
        assertNotNull(guildRepository.findGuildByMember(bob.uniqueId))
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

    @Test
    fun `Delete guild - Should delete when called by leader`() {
        // Given
        val alice = player()
        val aliceGuild = guild(alice)

        // When
        val ok = guildService.deleteGuildAsLeader(alice.uniqueId)

        // Then
        assertTrue(ok)
        assertNull(guildRepository.getById(aliceGuild.id))
    }

    @Test
    fun `Delete guild - Should not delete when caller is not leader`() {
        // Given
        val alice = player()
        val bob = player()
        val aliceGuild = guild(alice)

        // When
        val ok = guildService.deleteGuildAsLeader(bob.uniqueId)

        // Then
        assertFalse(ok)
        assertNotNull(guildRepository.getById(aliceGuild.id))
    }

    @Test
    fun `Leave guild - Should success - When member is not leader`() {
        // Given
        val alice = player()
        val bob = player()
        val aliceGuild = guild(alice)

        assertEquals(InvitationResult.Invited, guildService.invite(aliceGuild.id, bob.uniqueId))
        assertEquals(JoinResult.Joined, guildService.join(aliceGuild.id, bob.uniqueId))

        // When
        val leaveResult = guildService.leaveGuild(bob.uniqueId)

        // Then
        assertEquals(LeaveResult.Success, leaveResult)
        assertNull(guildRepository.findGuildByMember(bob.uniqueId))
    }

    @Test
    fun `Leave guild - Should not success - When member is leader`() {
        // Given
        val alice = player()
        val bob = player()
        val aliceGuild = guild(alice)

        assertEquals(InvitationResult.Invited, guildService.invite(aliceGuild.id, bob.uniqueId))
        assertEquals(JoinResult.Joined, guildService.join(aliceGuild.id, bob.uniqueId))

        // When
        val leaveResult = guildService.leaveGuild(alice.uniqueId)

        // Then
        assertEquals(LeaveResult.LeaderMustDelete, leaveResult)
        assertNotNull(guildRepository.findGuildByMember(alice.uniqueId))
    }

    @Test
    fun `Claim - Should return NOT_AUTHORIZED - When requester is not leader`() {
        // Given
        val alice = player(money = 10_000)
        val world = mockWorld(SrpWorld.GuildWorld.bukkitName)
        val loc = Location(world, 500.0, 64.0, 500.0)
        val aliceGuild = guildService.createGuild(alice.uniqueId, loc, "Alpha").guild!!
        val bob = player()
        assertEquals(InvitationResult.Invited, guildService.invite(aliceGuild.id, bob.uniqueId))
        assertEquals(JoinResult.Joined, guildService.join(aliceGuild.id, bob.uniqueId))
        val maxX = aliceGuild.chunks.maxOf { it.x }
        val z0 = aliceGuild.chunks.first().z
        val target = mock(org.bukkit.Chunk::class.java).apply {
            `when`(x).thenReturn(maxX + 1)
            `when`(z).thenReturn(z0)
        }

        // When
        val res = guildService.claimChunk(aliceGuild.id, bob.uniqueId, target)

        // Then
        assertEquals(GuildClaimResult.NOT_AUTHORIZED, res)
    }

    @Test
    fun `Claim - Should return ALREADY_OWNED - When chunk already belongs to guild`() {
        // Given
        val alice = player(money = 10_000)
        val world = mockWorld(SrpWorld.GuildWorld.bukkitName)
        val loc = Location(world, 600.0, 64.0, 600.0)
        val aliceGuild = guildService.createGuild(alice.uniqueId, loc, "Alpha").guild!!
        val owned = aliceGuild.chunks.first()
        val chunk = mock(org.bukkit.Chunk::class.java).apply {
            `when`(x).thenReturn(owned.x)
            `when`(z).thenReturn(owned.z)
        }

        // When
        val res = guildService.claimChunk(aliceGuild.id, alice.uniqueId, chunk)

        // Then
        assertEquals(GuildClaimResult.ALREADY_OWNED, res)
    }

    @Test
    fun `Claim - Should return ALREADY_CLAIMED - When chunk belongs to another guild`() {
        // Given
        val alice = player(money = 10_000)
        val bob   = player(money = 10_000)
        val world = mockWorld(SrpWorld.GuildWorld.bukkitName)

        val aliceGuild = guildService.createGuild(alice.uniqueId, Location(world, 700.0, 64.0, 700.0), "Alpha").guild!!
        val bobGuild = guildService.createGuild(bob.uniqueId,   Location(world, 900.0, 64.0, 900.0), "Beta").guild!!

        val theirs = bobGuild.chunks.first()
        val chunk = mock(org.bukkit.Chunk::class.java).apply {
            `when`(x).thenReturn(theirs.x)
            `when`(z).thenReturn(theirs.z)
        }

        // When
        val res = guildService.claimChunk(aliceGuild.id, alice.uniqueId, chunk)

        // Then
        assertEquals(GuildClaimResult.ALREADY_CLAIMED, res)
    }

    @Test
    fun `Claim - Should return NOT_ADJACENT - When target is not adjacent to any owned chunk`() {
        // Given
        val alice = player(money = 10_000)
        val world = mockWorld(SrpWorld.GuildWorld.bukkitName)
        val loc = Location(world, 1000.0, 64.0, 1000.0)
        val aliceGuild = guildService.createGuild(alice.uniqueId, loc, "Alpha").guild!!
        val maxX = aliceGuild.chunks.maxOf { it.x }
        val z0 = aliceGuild.chunks.first().z
        val chunk = mock(org.bukkit.Chunk::class.java).apply {
            `when`(x).thenReturn(maxX + 2) // non adjacent
            `when`(z).thenReturn(z0)
        }

        // When
        val res = guildService.claimChunk(aliceGuild.id, alice.uniqueId, chunk)

        // Then
        assertEquals(GuildClaimResult.NOT_ADJACENT, res)
    }

    @Test
    fun `Claim - Should return SUCCESS - When target is adjacent and free`() {
        // Given
        val alice = player(money = 10_000)
        val world = mockWorld(SrpWorld.GuildWorld.bukkitName)
        val loc = Location(world, 1200.0, 64.0, 1200.0)
        val aliceGuild = guildService.createGuild(alice.uniqueId, loc, "Alpha").guild!!
        val maxX = aliceGuild.chunks.maxOf { it.x }
        val z0 = aliceGuild.chunks.first().z
        val chunk = mock(org.bukkit.Chunk::class.java).apply {
            `when`(x).thenReturn(maxX + 1) // adjacent
            `when`(z).thenReturn(z0)
        }

        // When
        val res = guildService.claimChunk(aliceGuild.id, alice.uniqueId, chunk)

        // Then
        assertEquals(GuildClaimResult.SUCCESS, res)
        val updated = guildRepository.getById(aliceGuild.id)!!
        assertTrue(updated.chunks.any { it.x == maxX + 1 && it.z == z0 })
    }

    @Test
    fun `Unclaim - Should return NOT_AUTHORIZED - When requester is not leader`() {
        // Given
        val alice = player(money = 10_000)
        val world = mockWorld(SrpWorld.GuildWorld.bukkitName)
        val loc = Location(world, 500.0, 64.0, 500.0)
        val aliceGuild = guildService.createGuild(alice.uniqueId, loc, "Alpha").guild!!
        val bob = player()
        assertEquals(InvitationResult.Invited, guildService.invite(aliceGuild.id, bob.uniqueId))
        assertEquals(JoinResult.Joined, guildService.join(aliceGuild.id, bob.uniqueId))
        val spawn = loc.chunk
        val ownedNonSpawn = aliceGuild.chunks.first { it.x != spawn.x || it.z != spawn.z }
        val target = mock(org.bukkit.Chunk::class.java).apply {
            `when`(x).thenReturn(ownedNonSpawn.x)
            `when`(z).thenReturn(ownedNonSpawn.z)
        }

        // When
        val res = guildService.unclaimChunk(aliceGuild.id, bob.uniqueId, target)

        // Then
        assertEquals(GuildUnclaimResult.NOT_AUTHORIZED, res)
    }

    @Test
    fun `Unclaim - Should return ALREADY_CLAIMED - When chunk does not belong to guild`() {
        // Given
        val alice = player(money = 10_000)
        val world = mockWorld(SrpWorld.GuildWorld.bukkitName)
        val loc = Location(world, 600.0, 64.0, 600.0)
        val aliceGuild = guildService.createGuild(alice.uniqueId, loc, "Beta").guild!!
        val farX = aliceGuild.chunks.maxOf { it.x } + 10
        val z0 = aliceGuild.chunks.first().z
        val target = mock(org.bukkit.Chunk::class.java).apply {
            `when`(x).thenReturn(farX)
            `when`(z).thenReturn(z0)
        }

        // When
        val res = guildService.unclaimChunk(aliceGuild.id, alice.uniqueId, target)

        // Then
        assertEquals(GuildUnclaimResult.ALREADY_CLAIMED, res)
    }

    @Test
    fun `Unclaim - Should return SPAWNPOINT_CHUNK - When trying to remove spawn chunk`() {
        // Given
        val alice = player(money = 10_000)
        val world = mockWorld(SrpWorld.GuildWorld.bukkitName)
        val loc = Location(world, 700.0, 64.0, 700.0)
        val aliceGuild = guildService.createGuild(alice.uniqueId, loc, "Gamma").guild!!
        val spawnChunkX = loc.blockX shr 4
        val spawnChunkZ = loc.blockZ shr 4
        val target = mock(org.bukkit.Chunk::class.java).apply {
            `when`(x).thenReturn(spawnChunkX)
            `when`(z).thenReturn(spawnChunkZ)
        }

        // When
        val res = guildService.unclaimChunk(aliceGuild.id, alice.uniqueId, target)

        // Then
        assertEquals(GuildUnclaimResult.SPAWNPOINT_CHUNK, res)
    }

    @Test
    fun `Unclaim - Should return LAST_CHUNK - When trying to remove the only remaining chunk`() {
        // Given
        val alice = player(money = 10_000)
        val world = mockWorld(SrpWorld.GuildWorld.bukkitName)
        val loc = Location(world, 0.0, 64.0, 0.0)
        val aliceGuild = Guild(id = -1, leaderId = alice.uniqueId, name = "Solo", spawnLocation = loc)
        val saved = guildRepository.save(aliceGuild)
        val onlyChunk = GuildChunk(5, 5)
        saved.addChunk(onlyChunk)
        guildRepository.save(saved)

        val target = mock(org.bukkit.Chunk::class.java).apply {
            `when`(x).thenReturn(onlyChunk.x)
            `when`(z).thenReturn(onlyChunk.z)
        }

        // When
        val res = guildService.unclaimChunk(saved.id, alice.uniqueId, target)

        // Then
        assertEquals(GuildUnclaimResult.LAST_CHUNK, res)
    }

    @Test
    fun `Unclaim - Should return SUCCESS - When removing an outer owned chunk keeping territory connected`() {
        // Given
        val alice = player(money = 10_000)
        val world = mockWorld(SrpWorld.GuildWorld.bukkitName)
        val loc = Location(world, 800.0, 64.0, 800.0)
        val aliceGuild = guildService.createGuild(alice.uniqueId, loc, "Delta").guild!!
        val spawn = loc.chunk
        val cornerX = spawn.x + 1
        val cornerZ = spawn.z + 1
        check(aliceGuild.chunks.any { it.x == cornerX && it.z == cornerZ })

        val target = mock(org.bukkit.Chunk::class.java).apply {
            `when`(x).thenReturn(cornerX)
            `when`(z).thenReturn(cornerZ)
        }

        // When
        val res = guildService.unclaimChunk(aliceGuild.id, alice.uniqueId, target)

        // Then
        assertEquals(GuildUnclaimResult.SUCCESS, res)
        val updated = guildRepository.getById(aliceGuild.id)!!
        assertFalse(updated.chunks.any { it.x == cornerX && it.z == cornerZ })
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
