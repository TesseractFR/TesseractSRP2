package onl.tesseract.srp.service.job.mission

import onl.tesseract.srp.domain.item.CustomMaterial
import onl.tesseract.srp.domain.job.EnumJob
import onl.tesseract.srp.domain.job.mission.JobMission
import onl.tesseract.srp.exception.PlayerNotConnectedException
import onl.tesseract.srp.repository.generic.job.JobMissionRepository
import onl.tesseract.srp.service.item.CustomItemService
import onl.tesseract.srp.service.job.JobService
import onl.tesseract.srp.service.job.PlayerJobService
import org.bukkit.entity.Player
import org.bukkit.inventory.PlayerInventory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.util.*

class JobMissionServiceTest {

    private lateinit var service: JobMissionService

    private lateinit var jobMissionRepository: JobMissionRepository

    private lateinit var customItemService: CustomItemService

    private lateinit var playerJobService: PlayerJobService
    private lateinit var player: Player

    @BeforeEach
    fun setup() {
        player = mock(Player::class.java)
        `when`(player.inventory).thenReturn(mock(PlayerInventory::class.java))
        `when`(player.uniqueId).thenReturn(UUID.randomUUID())
        `when`(player.isConnected).thenReturn(true)

        jobMissionRepository = JobMissionRepositoryStub()
        customItemService = mock(CustomItemService::class.java)
        playerJobService = mock(PlayerJobService::class.java)
        service = JobMissionService(
            jobMissionRepository,
            mock(JobService::class.java),
            playerJobService,
            customItemService,
        )
    }

    @Test
    fun `consumeItemsForMission - Mission does not exist - Should throw error`() {
        // Given

        // When / Then
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            service.consumeItemsForMission(player, 0L)
        }
    }

    @Test
    fun `consumeItemsForMission - Should remove items from inventory without completing mission`() {
        // Given
        val mission = JobMission(
            1L,
            playerId = UUID.randomUUID(),
            job = EnumJob.Mineur,
            material = CustomMaterial.Wood,
            32,
            20,
            delivered = 0,
            reward = 10
        )
        jobMissionRepository.save(mission)
        `when`(customItemService.removeCustomItems(player.inventory, CustomMaterial.Wood, 20, 32))
            .thenReturn(30)

        // When
        val (delivered, remaining) = service.consumeItemsForMission(player, 1L)

        // Then
        verify(customItemService).removeCustomItems(player.inventory, CustomMaterial.Wood, 20, 32)
        Assertions.assertEquals(30, delivered)
        Assertions.assertEquals(2, remaining)

        val saved = jobMissionRepository.getById(1L)
        Assertions.assertNotNull(saved)
        Assertions.assertEquals(30, saved!!.delivered)
    }

    @Test
    fun `consumeItemsForMission - Should remove items from inventory and complete mission`() {
        // Given
        val mission = JobMission(
            1L,
            playerId = UUID.randomUUID(),
            job = EnumJob.Mineur,
            material = CustomMaterial.Wood,
            32,
            20,
            delivered = 0,
            reward = 10
        )
        jobMissionRepository.save(mission)
        `when`(customItemService.removeCustomItems(player.inventory, CustomMaterial.Wood, 20, 32))
            .thenReturn(32)

        // When
        val (delivered, remaining) = service.consumeItemsForMission(player, 1L)

        // Then
        Assertions.assertEquals(32, delivered)
        Assertions.assertEquals(0, remaining)

        verify(customItemService).removeCustomItems(player.inventory, CustomMaterial.Wood, 20, 32)
        verify(playerJobService).increaseReputation(player.uniqueId, EnumJob.Mineur)

        val saved = jobMissionRepository.getById(1L)
        Assertions.assertNull(saved)
    }

    @Test
    fun `consumeItemsForMission - Should throw error - When mission is already completed`() {
        // Given
        val mission = JobMission(
            1L,
            playerId = UUID.randomUUID(),
            job = EnumJob.Mineur,
            material = CustomMaterial.Wood,
            32,
            20,
            delivered = 32,
            reward = 10
        )
        jobMissionRepository.save(mission)

        // When
        Assertions.assertThrows(IllegalStateException::class.java) {
            service.consumeItemsForMission(player, 1L)
        }
    }

    @Test
    fun `consumeItemsForMission - Should remove only missing items - When some items already delivered`() {
        // Given
        val mission = JobMission(
            1L,
            playerId = UUID.randomUUID(),
            job = EnumJob.Mineur,
            material = CustomMaterial.Wood,
            32,
            20,
            delivered = 10,
            reward = 10
        )
        jobMissionRepository.save(mission)
        `when`(customItemService.removeCustomItems(player.inventory, CustomMaterial.Wood, 20, 22))
            .thenReturn(22)

        // When
        val (delivered, remaining) = service.consumeItemsForMission(player, 1L)

        // Then
        verify(customItemService).removeCustomItems(player.inventory, CustomMaterial.Wood, 20, 22)
        Assertions.assertEquals(22, delivered)
        Assertions.assertEquals(0, remaining)

        val saved = jobMissionRepository.getById(1L)
        Assertions.assertNull(saved)
    }

    @Test
    fun `consumeItemsForMission - Should do nothing - When no items in inventory`() {
        // Given
        val mission = JobMission(
            1L,
            playerId = UUID.randomUUID(),
            job = EnumJob.Mineur,
            material = CustomMaterial.Wood,
            32,
            20,
            delivered = 10,
            reward = 10
        )
        jobMissionRepository.save(mission)
        `when`(customItemService.removeCustomItems(player.inventory, CustomMaterial.Wood, 20, 22))
            .thenReturn(0)

        // When
        val (delivered, remaining) = service.consumeItemsForMission(player, 1L)

        // Then
        verify(customItemService).removeCustomItems(player.inventory, CustomMaterial.Wood, 20, 22)
        Assertions.assertEquals(0, delivered)
        Assertions.assertEquals(22, remaining)
    }

    @Test
    fun `consumeItemsForMission - Should throw error - When player is not connected`() {
        // Given
        `when`(player.isConnected).thenReturn(false)

        // When / Then
        Assertions.assertThrows(PlayerNotConnectedException::class.java) {
            service.consumeItemsForMission(player, 1L)
        }
    }
}

data class JobMissionRepositoryStub(private val map: MutableMap<Long, JobMission> = mutableMapOf()) : JobMissionRepository {
    override fun getById(id: Long): JobMission? {
        return map[id]
    }

    override fun deleteById(id: Long) {
        map.remove(id)
    }

    override fun findAllByPlayerId(playerId: UUID): List<JobMission> {
        return map.values.filter { it.playerId == playerId }
    }

    override fun save(entity: JobMission): JobMission {
        map[entity.id] = entity
        return entity
    }

    override fun idOf(entity: JobMission): Long {
        return entity.id
    }
}