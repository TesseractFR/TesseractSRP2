package onl.tesseract.srp.commission.domain.port.userside;

import onl.tesseract.srp.commission.domain.model.board.BoardId;
import onl.tesseract.srp.commission.domain.model.board.BoardLevels;
import onl.tesseract.srp.commission.domain.model.board.BoardPart;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.commission.domain.model.exception.CommissionBoardNotFoundException;
import onl.tesseract.srp.commission.domain.port.serverside.BoardFurniturePort;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionBoardRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionCatalogRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionConfigRepository;
import onl.tesseract.srp.commission.domain.port.serverside.GuildMembershipPort;
import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.common.domain.model.Coordinate;
import onl.tesseract.srp.common.domain.model.PlayerId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommissionBoardServiceTest {

    private static final BoardId GUILD_BOARD_ID = new BoardId(1);
    private static final BoardId SPAWN_BOARD_ID = new BoardId(2);
    private static final GuildId GUILD_ID = new GuildId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final PlayerId MEMBER = new PlayerId(UUID.fromString("00000000-0000-0000-0000-000000000010"));
    private static final PlayerId EXTERNAL = new PlayerId(UUID.fromString("00000000-0000-0000-0000-000000000020"));

    private static CommissionBoard guildBoard() {
        return CommissionBoard.guildBoard(GUILD_BOARD_ID, GUILD_ID, BoardLevels.GUILD_START, 0.1);
    }

    private CommissionBoardRepository commissionBoardRepository;
    private GuildMembershipPort guildMembershipPort;
    private BoardFurniturePort boardFurniturePort;
    private CommissionBoardRefillService commissionBoardRefillService;
    private CommissionBoardService commissionBoardService;

    @BeforeEach
    void setUp() {
        commissionBoardRepository = mock(CommissionBoardRepository.class);
        guildMembershipPort = mock(GuildMembershipPort.class);
        CommissionCatalogRepository commissionCatalogRepository = mock(CommissionCatalogRepository.class);
        boardFurniturePort = mock(BoardFurniturePort.class);
        CommissionConfigRepository commissionConfigRepository = mock(CommissionConfigRepository.class);
        commissionBoardRefillService = mock(CommissionBoardRefillService.class);
        commissionBoardService = new CommissionBoardService(commissionBoardRepository, guildMembershipPort, commissionCatalogRepository,
                boardFurniturePort, commissionConfigRepository, commissionBoardRefillService);

        when(commissionConfigRepository.getDefaultUsageTaxRate()).thenReturn(0.1);
        when(commissionConfigRepository.getSpawnSlotsPerType()).thenReturn(4);
    }

    @Test
    void getGuildBoard_createsTheBoardWithTheConfiguredTaxAndFillsIt() {
        // given
        CommissionBoard createdBoard = guildBoard();
        when(commissionBoardRepository.findByGuild(GUILD_ID)).thenReturn(Optional.empty());
        when(commissionBoardRepository.createGuildBoard(GUILD_ID, 0.1)).thenReturn(createdBoard);

        // when
        CommissionBoard guildBoard = commissionBoardService.getGuildBoard(GUILD_ID);

        // then
        assertSame(createdBoard, guildBoard);
        verify(commissionBoardRefillService).refillBoard(createdBoard);
    }

    @Test
    void getSpawnBoard_createsTheBoardWithTheConfiguredSlots() {
        // given
        CommissionBoard createdBoard = CommissionBoard.spawnBoard(SPAWN_BOARD_ID, BoardLevels.spawn(4));
        when(commissionBoardRepository.findSpawnBoard()).thenReturn(Optional.empty());
        when(commissionBoardRepository.createSpawnBoard(BoardLevels.spawn(4))).thenReturn(createdBoard);

        // when
        CommissionBoard spawnBoard = commissionBoardService.getSpawnBoard();

        // then
        assertSame(createdBoard, spawnBoard);
        verify(commissionBoardRefillService).refillBoard(createdBoard);
    }

    @Test
    void getSpawnBoard_appliesTheConfiguredSlotsToTheExistingBoard() {
        // given : le tableau a été créé avec 6 slots, la configuration en demande maintenant 4
        CommissionBoard existingBoard = CommissionBoard.spawnBoard(SPAWN_BOARD_ID, BoardLevels.spawn(6));
        when(commissionBoardRepository.findSpawnBoard()).thenReturn(Optional.of(existingBoard));

        // when
        CommissionBoard spawnBoard = commissionBoardService.getSpawnBoard();

        // then
        assertEquals(4, spawnBoard.getLevels().slotsPerType());
        verify(commissionBoardRepository).save(existingBoard);
    }

    @Test
    void getSpawnBoard_savesNothingWhenTheSlotsAlreadyMatchTheConfiguration() {
        // given
        CommissionBoard existingBoard = CommissionBoard.spawnBoard(SPAWN_BOARD_ID, BoardLevels.spawn(4));
        when(commissionBoardRepository.findSpawnBoard()).thenReturn(Optional.of(existingBoard));

        // when
        commissionBoardService.getSpawnBoard();

        // then
        verify(commissionBoardRepository, never()).save(any());
    }

    @Test
    void getBoard_throwsWhenTheBoardDoesNotExist() {
        // given
        when(commissionBoardRepository.findById(GUILD_BOARD_ID)).thenReturn(Optional.empty());

        // when / then
        assertThrows(CommissionBoardNotFoundException.class, () -> commissionBoardService.getBoard(GUILD_BOARD_ID));
    }

    @Test
    void canAccessPart_opensTheGuildPartToMembersOnly() {
        // given
        CommissionBoard guildBoard = guildBoard();
        when(guildMembershipPort.isMember(GUILD_ID, MEMBER)).thenReturn(true);

        // when / then
        assertTrue(commissionBoardService.canAccessPart(MEMBER, guildBoard, BoardPart.GUILD));
        assertFalse(commissionBoardService.canAccessPart(EXTERNAL, guildBoard, BoardPart.GUILD));
        assertTrue(commissionBoardService.canAccessPart(EXTERNAL, guildBoard, BoardPart.PUBLIC));
    }

    @Test
    void canManageBoard_isAlwaysFalseOnTheSpawnBoard() {
        // given
        CommissionBoard spawnBoard = CommissionBoard.spawnBoard(SPAWN_BOARD_ID, BoardLevels.spawn(4));

        // when / then
        assertFalse(commissionBoardService.canManageBoard(MEMBER, spawnBoard));
        verify(guildMembershipPort, never()).canManageBoard(any(), any());
    }

    @Test
    void deleteGuildBoard_removesTheFurnitureThenTheBoard() {
        // given
        Coordinate furniturePosition = new Coordinate(0.0, 64.0, 0.0, new ChunkCoord(0, 0, "world"));
        CommissionBoard guildBoard = guildBoard();
        guildBoard.placeFurnitureAt(furniturePosition);
        when(commissionBoardRepository.findByGuild(GUILD_ID)).thenReturn(Optional.of(guildBoard));

        // when
        commissionBoardService.deleteGuildBoard(GUILD_ID);

        // then
        verify(boardFurniturePort).removeFurniture(furniturePosition);
        verify(commissionBoardRepository).deleteByGuild(GUILD_ID);
    }
    @Test
    void findAllGuildMembers_listsTheMembersOfTheOwnerGuildAndNobodyAtTheSpawn() {
        // given
        when(guildMembershipPort.findAllMembers(GUILD_ID)).thenReturn(List.of(MEMBER));
        CommissionBoard spawnBoard = CommissionBoard.spawnBoard(SPAWN_BOARD_ID, BoardLevels.spawn(4));

        // when / then
        assertEquals(List.of(MEMBER), commissionBoardService.findAllGuildMembers(guildBoard()));
        assertEquals(List.of(), commissionBoardService.findAllGuildMembers(spawnBoard));
    }
}
