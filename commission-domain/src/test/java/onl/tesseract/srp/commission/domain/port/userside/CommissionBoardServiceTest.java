package onl.tesseract.srp.commission.domain.port.userside;

import onl.tesseract.srp.commission.domain.model.board.UsageTaxRate;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardId;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardLevels;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardPart;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.commission.domain.model.commission.Commission;
import onl.tesseract.srp.commission.domain.model.commission.CommissionId;
import onl.tesseract.srp.commission.domain.model.commission.CommissionType;
import onl.tesseract.srp.commission.domain.model.exception.CommissionBoardNotFoundException;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionBoardRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionCatalogRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionConfigRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionRepository;
import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommissionBoardServiceTest {

    private static final CommissionBoardId GUILD_BOARD_ID = new CommissionBoardId(1);
    private static final CommissionBoardId SPAWN_BOARD_ID = new CommissionBoardId(2);
    private static final GuildId GUILD_ID = new GuildId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final PlayerId EXTERNAL = new PlayerId(UUID.fromString("00000000-0000-0000-0000-000000000020"));

    private static CommissionBoard guildBoard() {
        return CommissionBoard.guildBoard(GUILD_BOARD_ID, GUILD_ID, CommissionBoardLevels.GUILD_BOARD_START, new UsageTaxRate(10));
    }

    private CommissionBoardRepository commissionBoardRepository;
    private CommissionRepository commissionRepository;
    private CommissionBoardAccessService commissionBoardAccessService;
    private CommissionBoardRefillService commissionBoardRefillService;
    private CommissionBoardService commissionBoardService;

    @BeforeEach
    void setUp() {
        commissionBoardRepository = mock(CommissionBoardRepository.class);
        commissionRepository = mock(CommissionRepository.class);
        commissionBoardAccessService = mock(CommissionBoardAccessService.class);
        CommissionCatalogRepository commissionCatalogRepository = mock(CommissionCatalogRepository.class);
        CommissionConfigRepository commissionConfigRepository = mock(CommissionConfigRepository.class);
        commissionBoardRefillService = mock(CommissionBoardRefillService.class);
        commissionBoardService = new CommissionBoardService(commissionBoardRepository, commissionRepository, commissionCatalogRepository,
                commissionConfigRepository, commissionBoardAccessService, commissionBoardRefillService);

        when(commissionConfigRepository.getDefaultUsageTaxRate()).thenReturn(new UsageTaxRate(10));
        when(commissionConfigRepository.getSpawnSlotsPerType()).thenReturn(4);
    }

    @Test
    void openGuildBoard_createsTheBoardWithTheConfiguredTaxAndFillsIt() {
        // given
        CommissionBoard createdBoard = guildBoard();
        when(commissionBoardRepository.findByGuild(GUILD_ID)).thenReturn(Optional.empty());
        when(commissionBoardRepository.createGuildBoard(GUILD_ID, new UsageTaxRate(10))).thenReturn(createdBoard);

        // when
        CommissionBoard guildBoard = commissionBoardService.openGuildBoard(GUILD_ID);

        // then
        assertSame(createdBoard, guildBoard);
        verify(commissionBoardRefillService).refillBoard(createdBoard);
    }

    @Test
    void openSpawnBoard_createsTheBoardWithTheConfiguredSlots() {
        // given
        CommissionBoard createdBoard = CommissionBoard.spawnBoard(SPAWN_BOARD_ID, 4);
        when(commissionBoardRepository.findSpawnBoard()).thenReturn(Optional.empty());
        when(commissionBoardRepository.createSpawnBoard(4)).thenReturn(createdBoard);

        // when
        CommissionBoard spawnBoard = commissionBoardService.openSpawnBoard();

        // then
        assertSame(createdBoard, spawnBoard);
        verify(commissionBoardRefillService).refillBoard(createdBoard);
    }

    @Test
    void openSpawnBoard_appliesTheConfiguredSlotsToTheExistingBoard() {
        // given : le tableau a été créé avec 6 slots, la configuration en demande maintenant 4
        CommissionBoard existingBoard = CommissionBoard.spawnBoard(SPAWN_BOARD_ID, 6);
        when(commissionBoardRepository.findSpawnBoard()).thenReturn(Optional.of(existingBoard));

        // when
        CommissionBoard spawnBoard = commissionBoardService.openSpawnBoard();

        // then
        assertEquals(CommissionBoardLevels.forSpawnBoard(4), spawnBoard.getLevels());
        verify(commissionBoardRepository).save(existingBoard);
    }

    @Test
    void openSpawnBoard_savesNothingWhenTheSlotsAlreadyMatchTheConfiguration() {
        // given
        CommissionBoard existingBoard = CommissionBoard.spawnBoard(SPAWN_BOARD_ID, 4);
        when(commissionBoardRepository.findSpawnBoard()).thenReturn(Optional.of(existingBoard));

        // when
        commissionBoardService.openSpawnBoard();

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
    void findAllVisibleCommissions_hidesTheGuildPartFromAnExternalPlayer() {
        // given
        CommissionBoard guildBoard = guildBoard();
        Commission publicCommission = commission(new CommissionId(1), CommissionBoardPart.PUBLIC);
        Commission guildCommission = commission(new CommissionId(2), CommissionBoardPart.GUILD);
        when(commissionRepository.findAllByBoard(GUILD_BOARD_ID)).thenReturn(List.of(publicCommission, guildCommission));
        when(commissionBoardAccessService.canAccessPart(EXTERNAL, guildBoard, CommissionBoardPart.PUBLIC)).thenReturn(true);
        when(commissionBoardAccessService.canAccessPart(EXTERNAL, guildBoard, CommissionBoardPart.GUILD)).thenReturn(false);

        // when
        List<Commission> visibleCommissions = commissionBoardService.findAllVisibleCommissions(EXTERNAL, guildBoard);

        // then
        assertEquals(List.of(publicCommission), visibleCommissions);
    }

    private static Commission commission(CommissionId commissionId, CommissionBoardPart part) {
        return new Commission(commissionId, GUILD_BOARD_ID, part, CommissionType.HARVEST, new MaterialName("BOIS"), 10, 0,
                Quality.NORMAL, MoneyValue.of(100));
    }

    @Test
    void deleteGuildBoard_deletesTheBoardOfTheGuild() {
        // when
        commissionBoardService.deleteGuildBoard(GUILD_ID);

        // then
        verify(commissionBoardRepository).deleteByGuild(GUILD_ID);
    }
}
