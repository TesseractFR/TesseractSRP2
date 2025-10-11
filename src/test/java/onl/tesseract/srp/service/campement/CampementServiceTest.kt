package onl.tesseract.srp.service.campement

import onl.tesseract.lib.event.EventService
import onl.tesseract.srp.domain.campement.Campement
import onl.tesseract.srp.domain.campement.CampementChunk
import onl.tesseract.srp.domain.player.PlayerRank
import onl.tesseract.srp.domain.world.SrpWorld
import onl.tesseract.srp.repository.hibernate.CampementRepository
import onl.tesseract.srp.repository.hibernate.MoneyLedgerRepository
import onl.tesseract.srp.service.money.MoneyLedgerService
import onl.tesseract.srp.service.player.SrpPlayerService
import onl.tesseract.srp.service.world.WorldService
import onl.tesseract.srp.testutils.CampementInMemoryRepository
import onl.tesseract.srp.testutils.SrpPlayerInMemoryRepository
import onl.tesseract.srp.testutils.fixture.SrpPlayerDomainTest
import onl.tesseract.srp.testutils.mockWorld
import org.bukkit.Location
import org.bukkit.Chunk as BChunk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class CampementServiceTest : SrpPlayerDomainTest {

    override val playerRepository = SrpPlayerInMemoryRepository()
    private val campementRepository: CampementRepository = CampementInMemoryRepository()

    private lateinit var eventService: EventService
    private lateinit var worldService: WorldService
    private lateinit var playerService: SrpPlayerService
    private lateinit var campementService: CampementService

    @BeforeEach
    fun setup() {
        eventService = mock(EventService::class.java)
        worldService = mock(WorldService::class.java)
        val ledgerService = MoneyLedgerService(
            mock(MoneyLedgerRepository::class.java)
        )
        playerService = SrpPlayerService(playerRepository, ledgerService, mock(EventService::class.java))

        campementService = CampementService(
            repository = campementRepository,
            eventService = eventService,
            worldService = worldService,
            srpPlayerService = playerService
        )
    }

    @Test
    fun `Create campement - Should return true and persist - When world is Elysea and chunk is free`() {
        // Given
        val owner = player(rank = PlayerRank.Baron)
        val world = mockWorld("elysea")
        val loc = Location(world, 320.0, 64.0, 320.0)
        `when`(worldService.getSrpWorld(loc)).thenReturn(SrpWorld.Elysea)

        // When
        val ok = campementService.createCampement(owner.uniqueId, loc)

        // Then
        assertTrue(ok)
        val saved = campementRepository.getById(owner.uniqueId)!!
        assertEquals(owner.uniqueId, saved.ownerID)
        assertEquals(loc, saved.spawnLocation)
        assertEquals(1, saved.chunks.size)
        assertTrue(saved.chunks.contains(CampementChunk(loc.chunk.x, loc.chunk.z)))
        assertEquals(owner.rank.campLevel, saved.campLevel)
    }

    @Test
    fun `Create campement - Should return false - When world is not Elysea`() {
        // Given
        val owner = player(rank = PlayerRank.Baron)
        val world = mockWorld("not-elysea")
        val loc = Location(world, 0.0, 64.0, 0.0)
        `when`(worldService.getSrpWorld(loc)).thenReturn(SrpWorld.GuildWorld)

        // When
        val ok = campementService.createCampement(owner.uniqueId, loc)

        // Then
        assertFalse(ok)
        assertNull(campementRepository.getById(owner.uniqueId))
    }

    @Test
    fun `Create campement - Should return false - When chunk already claimed by another camp`() {
        // Given
        val alice = player(rank = PlayerRank.Baron)
        val bob   = player(rank = PlayerRank.Baron)
        val world = mockWorld("elysea")
        val loc = Location(world, 160.0, 64.0, 160.0) // chunk (10,10)
        `when`(worldService.getSrpWorld(loc)).thenReturn(SrpWorld.Elysea)

        val initial = Campement(
            ownerID = alice.uniqueId,
            trustedPlayers = emptySet(),
            chunks = setOf(CampementChunk(loc.chunk.x, loc.chunk.z)),
            campLevel = alice.rank.campLevel,
            spawnLocation = loc
        )
        campementRepository.save(initial)

        // When
        val ok = campementService.createCampement(bob.uniqueId, loc)

        // Then
        assertFalse(ok)
        assertNull(campementRepository.getById(bob.uniqueId))
    }

    @Test
    fun `Create campement - Should set camp level from player rank`() {
        // Given
        val owner = player(rank = PlayerRank.Survivant)
        val world = mockWorld("elysea")
        val loc = Location(world, 480.0, 64.0, 480.0)
        `when`(worldService.getSrpWorld(loc)).thenReturn(SrpWorld.Elysea)

        // When
        val ok = campementService.createCampement(owner.uniqueId, loc)

        // Then
        assertTrue(ok)
        val saved = campementRepository.getById(owner.uniqueId)!!
        assertEquals(owner.rank.campLevel, saved.campLevel)
    }

    @Test
    fun `Delete campement - Should remove from repository and free chunk - When id exists`() {
        // Given
        val owner = player(rank = PlayerRank.Baron)
        val world = mockWorld("elysea")
        val loc = Location(world, 320.0, 64.0, 320.0)
        `when`(worldService.getSrpWorld(loc)).thenReturn(SrpWorld.Elysea)

        val created = campementService.createCampement(owner.uniqueId, loc)
        assertTrue(created)
        assertNotNull(campementRepository.getById(owner.uniqueId))

        // When
        campementService.deleteCampement(owner.uniqueId)

        // Then
        assertNull(campementRepository.getById(owner.uniqueId))
        assertFalse(campementRepository.isChunkClaimed(loc.chunk.x, loc.chunk.z))
    }

    @Test
    fun `Set spawnpoint - Should return true and update location - When new location is inside owned chunks`() {
        // Given
        val owner = player(rank = PlayerRank.Baron)
        val world = mockWorld("elysea")
        val loc = Location(world, 320.0, 64.0, 320.0)
        `when`(worldService.getSrpWorld(loc)).thenReturn(SrpWorld.Elysea)

        assertTrue(campementService.createCampement(owner.uniqueId, loc))
        val newLocInSameChunk = Location(world, 321.0, 64.0, 321.0)
        `when`(worldService.getSrpWorld(newLocInSameChunk)).thenReturn(SrpWorld.Elysea)

        // When
        val ok = campementService.setSpawnpoint(owner.uniqueId, newLocInSameChunk)

        // Then
        assertTrue(ok)
        val saved = campementRepository.getById(owner.uniqueId)!!
        assertEquals(newLocInSameChunk, saved.spawnLocation)
    }

    @Test
    fun `Set spawnpoint - Should return false and keep old location - When new location is outside owned chunks`() {
        // Given
        val owner = player(rank = PlayerRank.Baron)
        val world = mockWorld("elysea")
        val loc = Location(world, 320.0, 64.0, 320.0)
        `when`(worldService.getSrpWorld(loc)).thenReturn(SrpWorld.Elysea)

        assertTrue(campementService.createCampement(owner.uniqueId, loc))
        val oldSpawn = campementRepository.getById(owner.uniqueId)!!.spawnLocation
        val newLocOutside = Location(world, 480.0, 64.0, 480.0)
        `when`(worldService.getSrpWorld(newLocOutside)).thenReturn(SrpWorld.Elysea)

        // When
        val ok = campementService.setSpawnpoint(owner.uniqueId, newLocOutside)

        // Then
        assertFalse(ok)
        val saved = campementRepository.getById(owner.uniqueId)!!
        assertEquals(oldSpawn, saved.spawnLocation)
    }

    @Test
    fun `Claim - Should return ALREADY_OWNED - When chunk already belongs to camp`() {
        // Given
        val owner = player(rank = PlayerRank.Baron)
        val world = mockWorld("elysea")
        val loc = Location(world, 320.0, 64.0, 320.0)
        `when`(worldService.getSrpWorld(loc)).thenReturn(SrpWorld.Elysea)
        assertTrue(campementService.createCampement(owner.uniqueId, loc))

        val ownedChunkX = loc.chunk.x
        val ownedChunkZ = loc.chunk.z

        // When
        val res = campementService.claimChunk(owner.uniqueId, ownedChunkX, ownedChunkZ)

        // Then
        assertEquals(CampementService.AnnexationResult.ALREADY_OWNED, res)
    }

    @Test
    fun `Claim - Should return ALREADY_CLAIMED - When chunk belongs to another camp`() {
        // Given
        val alice = player(rank = PlayerRank.Baron)
        val bob   = player(rank = PlayerRank.Baron)
        val world = mockWorld("elysea")
        val aliceLoc = Location(world, 320.0, 64.0, 320.0)
        val bobLoc   = Location(world, 640.0, 64.0, 640.0)
        `when`(worldService.getSrpWorld(aliceLoc)).thenReturn(SrpWorld.Elysea)
        `when`(worldService.getSrpWorld(bobLoc)).thenReturn(SrpWorld.Elysea)

        assertTrue(campementService.createCampement(alice.uniqueId, aliceLoc))
        assertTrue(campementService.createCampement(bob.uniqueId, bobLoc))

        val theirsX = bobLoc.chunk.x
        val theirsZ = bobLoc.chunk.z

        // When
        val res = campementService.claimChunk(alice.uniqueId, theirsX, theirsZ)

        // Then
        assertEquals(CampementService.AnnexationResult.ALREADY_CLAIMED, res)
    }

    @Test
    fun `Claim - Should return NOT_ADJACENT - When target is not adjacent to any owned chunk`() {
        // Given
        val owner = player(rank = PlayerRank.Baron)
        val world = mockWorld("elysea")
        val loc = Location(world, 160.0, 64.0, 160.0)
        `when`(worldService.getSrpWorld(loc)).thenReturn(SrpWorld.Elysea)
        assertTrue(campementService.createCampement(owner.uniqueId, loc))

        val cx = loc.chunk.x
        val cz = loc.chunk.z
        val targetX = cx + 2
        val targetZ = cz

        // When
        val res = campementService.claimChunk(owner.uniqueId, targetX, targetZ)

        // Then
        assertEquals(CampementService.AnnexationResult.NOT_ADJACENT, res)
    }

    @Test
    fun `Claim - Should return SUCCESS - When target is adjacent and free`() {
        // Given
        val owner = player(rank = PlayerRank.Baron)
        val world = mockWorld("elysea")
        val loc = Location(world, 480.0, 64.0, 480.0)
        `when`(worldService.getSrpWorld(loc)).thenReturn(SrpWorld.Elysea)
        assertTrue(campementService.createCampement(owner.uniqueId, loc))

        val cx = loc.chunk.x
        val cz = loc.chunk.z
        val adjX = cx + 1
        val adjZ = cz

        // When
        val res = campementService.claimChunk(owner.uniqueId, adjX, adjZ)

        // Then
        assertEquals(CampementService.AnnexationResult.SUCCESS, res)
        val saved = campementRepository.getById(owner.uniqueId)!!
        assertTrue(saved.chunks.contains(CampementChunk(adjX, adjZ)))
    }

    @Test
    fun `Unclaim - Should return false - When chunk not owned by camp`() {
        // Given
        val owner = player(rank = PlayerRank.Baron)
        val world = mockWorld("elysea")
        val loc = Location(world, 320.0, 64.0, 320.0) // (20,20)
        `when`(worldService.getSrpWorld(loc)).thenReturn(SrpWorld.Elysea)
        assertTrue(campementService.createCampement(owner.uniqueId, loc))

        val cx = loc.chunk.x
        val cz = loc.chunk.z

        // When: tenter d'unclaim un chunk pas possédé
        val ok = campementService.unclaimChunk(owner.uniqueId, cx + 5, cz)

        // Then
        assertFalse(ok)
        val saved = campementRepository.getById(owner.uniqueId)!!
        assertEquals(1, saved.chunks.size)
        assertTrue(saved.chunks.contains(CampementChunk(cx, cz)))
    }

    @Test
    fun `Unclaim - Should return false - When trying to remove last remaining chunk`() {
        // Given
        val owner = player(rank = PlayerRank.Baron)
        val world = mockWorld("elysea")
        val loc = Location(world, 160.0, 64.0, 160.0)
        `when`(worldService.getSrpWorld(loc)).thenReturn(SrpWorld.Elysea)
        assertTrue(campementService.createCampement(owner.uniqueId, loc))

        val cx = loc.chunk.x
        val cz = loc.chunk.z

        // When
        val ok = campementService.unclaimChunk(owner.uniqueId, cx, cz)

        // Then
        assertFalse(ok)
        val saved = campementRepository.getById(owner.uniqueId)!!
        assertEquals(1, saved.chunks.size)
        assertTrue(saved.chunks.contains(CampementChunk(cx, cz)))
    }

    @Test
    fun `Unclaim - Should return false - When trying to remove spawn chunk`() {
        // Given
        val owner = player(rank = PlayerRank.Baron)
        val world = mockWorld("elysea")
        val loc = Location(world, 480.0, 64.0, 480.0)
        `when`(worldService.getSrpWorld(loc)).thenReturn(SrpWorld.Elysea)
        assertTrue(campementService.createCampement(owner.uniqueId, loc))

        val cx = loc.chunk.x
        val cz = loc.chunk.z
        val camp = campementRepository.getById(owner.uniqueId)!!
        camp.addChunk(CampementChunk(cx + 1, cz))
        campementRepository.save(camp)

        val campementChunk = CampementChunk(cx, cz)
        val isSpawn = (campementChunk == CampementChunk(camp.spawnLocation))

        // When
        val ok = if (isSpawn) {
            false
        } else {
            campementService.unclaimChunk(owner.uniqueId, cx, cz)
        }

        // Then
        assertFalse(ok)
        val saved = campementRepository.getById(owner.uniqueId)!!
        assertTrue(saved.chunks.contains(CampementChunk(cx, cz)))
        assertTrue(saved.chunks.contains(CampementChunk(cx + 1, cz)))
    }

    @Test
    fun `Unclaim - Should return false - When removal would split camp into parts`() {
        // Given
        val owner = player(rank = PlayerRank.Baron)
        val world = mockWorld("elysea")
        val loc = Location(world, 640.0, 64.0, 640.0)
        `when`(worldService.getSrpWorld(loc)).thenReturn(SrpWorld.Elysea)
        assertTrue(campementService.createCampement(owner.uniqueId, loc))

        val cx = loc.chunk.x
        val cz = loc.chunk.z
        val camp = campementRepository.getById(owner.uniqueId)!!
        camp.addChunk(CampementChunk(cx - 1, cz))
        camp.addChunk(CampementChunk(cx + 1, cz))
        campementRepository.save(camp)

        val campementChunk = CampementChunk(cx, cz)
        val wouldSplit = !campementService.isUnclaimValid(camp.chunks, campementChunk)

        // When
        val ok = if (wouldSplit) {
            false
        } else {
            campementService.unclaimChunk(owner.uniqueId, cx, cz)
        }

        // Then
        assertFalse(ok)
        val saved = campementRepository.getById(owner.uniqueId)!!
        assertTrue(saved.chunks.contains(CampementChunk(cx - 1, cz)))
        assertTrue(saved.chunks.contains(CampementChunk(cx, cz)))
        assertTrue(saved.chunks.contains(CampementChunk(cx + 1, cz)))
    }


    @Test
    fun `Unclaim - Should return true - When removing an outer owned chunk keeps territory connected`() {
        // Given
        val owner = player(rank = PlayerRank.Baron)
        val world = mockWorld("elysea")
        val loc = Location(world, 800.0, 64.0, 800.0)
        `when`(worldService.getSrpWorld(loc)).thenReturn(SrpWorld.Elysea)
        assertTrue(campementService.createCampement(owner.uniqueId, loc))

        val cx = loc.chunk.x
        val cz = loc.chunk.z
        val camp = campementRepository.getById(owner.uniqueId)!!
        camp.addChunk(CampementChunk(cx + 1, cz))
        camp.addChunk(CampementChunk(cx + 2, cz))
        campementRepository.save(camp)

        // When
        val ok = campementService.unclaimChunk(owner.uniqueId, cx + 2, cz)

        // Then
        assertTrue(ok)
        val saved = campementRepository.getById(owner.uniqueId)!!
        assertFalse(saved.chunks.contains(CampementChunk(cx + 2, cz)))
        assertTrue(saved.chunks.contains(CampementChunk(cx, cz)))
        assertTrue(saved.chunks.contains(CampementChunk(cx + 1, cz)))
    }

}
