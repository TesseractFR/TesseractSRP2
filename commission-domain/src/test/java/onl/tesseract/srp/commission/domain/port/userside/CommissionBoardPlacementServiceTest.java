package onl.tesseract.srp.commission.domain.port.userside;

import onl.tesseract.srp.commission.domain.model.board.BoardFacing;
import onl.tesseract.srp.commission.domain.model.board.BoardId;
import onl.tesseract.srp.commission.domain.model.board.BoardLevels;
import onl.tesseract.srp.commission.domain.model.board.BoardSpot;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.commission.domain.model.exception.BoardFurnitureUnavailableException;
import onl.tesseract.srp.commission.domain.model.exception.BoardManagementNotAllowedException;
import onl.tesseract.srp.commission.domain.model.exception.BoardOutsideTerritoryException;
import onl.tesseract.srp.commission.domain.model.exception.BoardPlacementNotStartedException;
import onl.tesseract.srp.commission.domain.model.exception.BoardSpotNotFreeException;
import onl.tesseract.srp.commission.domain.port.serverside.BoardFurniturePort;
import onl.tesseract.srp.commission.domain.port.serverside.BoardPlacementSessionPort;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionBoardRepository;
import onl.tesseract.srp.commission.domain.port.serverside.GuildTerritoryPort;
import onl.tesseract.srp.common.domain.model.ChunkCoord;
import onl.tesseract.srp.common.domain.model.Coordinate;
import onl.tesseract.srp.common.domain.model.PlayerId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommissionBoardPlacementServiceTest {

    private static final BoardId GUILD_BOARD_ID = new BoardId(1);
    private static final GuildId GUILD_ID = new GuildId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final PlayerId MANAGER = new PlayerId(UUID.fromString("00000000-0000-0000-0000-000000000010"));
    private static final PlayerId EXTERNAL = new PlayerId(UUID.fromString("00000000-0000-0000-0000-000000000020"));
    private static final Coordinate OLD_POSITION = new Coordinate(0.0, 64.0, 0.0, new ChunkCoord(0, 0, "world"));
    private static final Coordinate NEW_POSITION = new Coordinate(5.0, 64.0, 5.0, new ChunkCoord(0, 0, "world"));
    private static final BoardSpot BOARD_SPOT = new BoardSpot(NEW_POSITION, BoardFacing.NORTH);

    private CommissionBoardService commissionBoardService;
    private CommissionBoardRepository commissionBoardRepository;
    private BoardPlacementSessionPort boardPlacementSessionPort;
    private BoardFurniturePort boardFurniturePort;
    private GuildTerritoryPort guildTerritoryPort;
    private CommissionBoardPlacementService commissionBoardPlacementService;
    private CommissionBoard guildBoard;

    @BeforeEach
    void setUp() {
        commissionBoardService = mock(CommissionBoardService.class);
        commissionBoardRepository = mock(CommissionBoardRepository.class);
        boardPlacementSessionPort = mock(BoardPlacementSessionPort.class);
        boardFurniturePort = mock(BoardFurniturePort.class);
        guildTerritoryPort = mock(GuildTerritoryPort.class);
        commissionBoardPlacementService = new CommissionBoardPlacementService(commissionBoardService, commissionBoardRepository,
                boardPlacementSessionPort, boardFurniturePort, guildTerritoryPort);
        guildBoard = CommissionBoard.guildBoard(GUILD_BOARD_ID, GUILD_ID, BoardLevels.GUILD_START, 0.25);
        guildBoard.placeFurnitureAt(OLD_POSITION);

        when(commissionBoardService.getGuildBoard(GUILD_ID)).thenReturn(guildBoard);
        when(commissionBoardService.canManageGuildBoard(MANAGER, GUILD_ID)).thenReturn(true);
        when(boardPlacementSessionPort.findGuildOfPlacingPlayer(MANAGER)).thenReturn(Optional.of(GUILD_ID));
        when(guildTerritoryPort.ownsChunk(any(), any())).thenReturn(true);
        when(boardFurniturePort.hasFreeSpace(BOARD_SPOT)).thenReturn(true);
        when(boardFurniturePort.placeFurniture(BOARD_SPOT)).thenReturn(Optional.of(NEW_POSITION));
    }

    @Test
    void startBoardPlacement_startsASessionForAManager() {
        // when
        commissionBoardPlacementService.startBoardPlacement(MANAGER, GUILD_ID);

        // then
        verify(boardPlacementSessionPort).startSession(MANAGER, GUILD_ID);
    }

    @Test
    void startBoardPlacement_throwsWhenThePlayerCannotManageTheBoard() {
        // when / then
        assertThrows(BoardManagementNotAllowedException.class, () -> commissionBoardPlacementService.startBoardPlacement(EXTERNAL, GUILD_ID));
        verify(boardPlacementSessionPort, never()).startSession(any(), any());
    }

    @Test
    void cancelBoardPlacement_endsTheSessionOfThePlayer() {
        // given
        when(boardPlacementSessionPort.endSession(MANAGER)).thenReturn(true);

        // when
        boolean isCancelled = commissionBoardPlacementService.cancelBoardPlacement(MANAGER);

        // then
        assertTrue(isCancelled);
    }

    @Test
    void placeGuildBoard_removesTheOldFurnitureAndSavesTheNewPosition() {
        // when
        commissionBoardPlacementService.placeGuildBoard(MANAGER, BOARD_SPOT);

        // then
        verify(boardFurniturePort).removeFurniture(OLD_POSITION);
        assertEquals(Optional.of(NEW_POSITION), guildBoard.findFurniturePosition());
        verify(commissionBoardRepository).save(guildBoard);
        verify(boardPlacementSessionPort).endSession(MANAGER);
    }

    @Test
    void placeGuildBoard_throwsWhenThePlayerIsNotPlacingABoard() {
        // when / then
        assertThrows(BoardPlacementNotStartedException.class, () -> commissionBoardPlacementService.placeGuildBoard(EXTERNAL, BOARD_SPOT));
    }

    @Test
    void placeGuildBoard_throwsAndKeepsTheOldBoardOutsideTheTerritory() {
        // given
        when(guildTerritoryPort.ownsChunk(any(), any())).thenReturn(false);

        // when / then
        assertThrows(BoardOutsideTerritoryException.class, () -> commissionBoardPlacementService.placeGuildBoard(MANAGER, BOARD_SPOT));
        verify(boardFurniturePort, never()).removeFurniture(any());
        verify(boardPlacementSessionPort, never()).endSession(any());
    }

    @Test
    void placeGuildBoard_throwsWhenTheSpotIsNotFree() {
        // given
        when(boardFurniturePort.hasFreeSpace(BOARD_SPOT)).thenReturn(false);

        // when / then
        assertThrows(BoardSpotNotFreeException.class, () -> commissionBoardPlacementService.placeGuildBoard(MANAGER, BOARD_SPOT));
        verify(boardFurniturePort, never()).placeFurniture(any());
    }

    @Test
    void placeGuildBoard_throwsAndKeepsTheOldBoardWhenTheFurnitureCannotBePlaced() {
        // given
        when(boardFurniturePort.placeFurniture(BOARD_SPOT)).thenReturn(Optional.empty());

        // when / then
        assertThrows(BoardFurnitureUnavailableException.class, () -> commissionBoardPlacementService.placeGuildBoard(MANAGER, BOARD_SPOT));
        verify(boardFurniturePort, never()).removeFurniture(any());
    }

    @Test
    void placeGuildBoard_placesNothingInTheWorldWhenTheBoardCannotBeLoaded() {
        // given
        when(commissionBoardService.getGuildBoard(GUILD_ID)).thenThrow(new IllegalArgumentException("configuration invalide"));

        // when / then
        assertThrows(IllegalArgumentException.class, () -> commissionBoardPlacementService.placeGuildBoard(MANAGER, BOARD_SPOT));
        verify(boardFurniturePort, never()).placeFurniture(any());
    }

    @Test
    void isBoardPlaced_isTrueOnlyOnceTheFurnitureHasAPosition() {
        // given
        when(commissionBoardRepository.findByGuild(GUILD_ID)).thenReturn(Optional.of(guildBoard));

        // when / then
        assertTrue(commissionBoardPlacementService.isBoardPlaced(GUILD_ID));
        when(commissionBoardRepository.findByGuild(GUILD_ID))
                .thenReturn(Optional.of(CommissionBoard.guildBoard(GUILD_BOARD_ID, GUILD_ID, BoardLevels.GUILD_START, 0.25)));
        assertFalse(commissionBoardPlacementService.isBoardPlaced(GUILD_ID));
    }
}
