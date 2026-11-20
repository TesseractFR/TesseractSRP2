package onl.tesseract.srp.commission.domain.port.userside;

import onl.tesseract.srp.commission.domain.model.board.BoardId;
import onl.tesseract.srp.commission.domain.model.board.BoardLevels;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.commission.domain.model.board.UpgradeStep;
import onl.tesseract.srp.commission.domain.model.board.UpgradeType;
import onl.tesseract.srp.commission.domain.model.exception.BoardManagementNotAllowedException;
import onl.tesseract.srp.commission.domain.model.exception.GuildLevelTooLowException;
import onl.tesseract.srp.commission.domain.model.exception.InvalidUsageTaxRateException;
import onl.tesseract.srp.commission.domain.model.exception.NotEnoughBankMoneyException;
import onl.tesseract.srp.commission.domain.model.exception.UpgradeMaxLevelReachedException;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionBoardRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionConfigRepository;
import onl.tesseract.srp.commission.domain.port.serverside.GuildProgressPort;
import onl.tesseract.srp.common.domain.model.PlayerId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommissionBoardManagementServiceTest {

    private static final BoardId GUILD_BOARD_ID = new BoardId(1);
    private static final BoardId SPAWN_BOARD_ID = new BoardId(2);
    private static final GuildId GUILD_ID = new GuildId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final PlayerId MANAGER = new PlayerId(UUID.fromString("00000000-0000-0000-0000-000000000010"));
    private static final PlayerId EXTERNAL = new PlayerId(UUID.fromString("00000000-0000-0000-0000-000000000020"));
    private static final double USAGE_TAX_RATE = 0.25;

    private CommissionBoardRepository commissionBoardRepository;
    private CommissionConfigRepository commissionConfigRepository;
    private GuildProgressPort guildProgressPort;
    private CommissionBoardService commissionBoardService;
    private CommissionBoardRefillService commissionBoardRefillService;
    private CommissionBoardManagementService commissionBoardManagementService;
    private CommissionBoard guildBoard;

    @BeforeEach
    void setUp() {
        commissionBoardRepository = mock(CommissionBoardRepository.class);
        commissionConfigRepository = mock(CommissionConfigRepository.class);
        guildProgressPort = mock(GuildProgressPort.class);
        commissionBoardService = mock(CommissionBoardService.class);
        commissionBoardRefillService = mock(CommissionBoardRefillService.class);
        commissionBoardManagementService = new CommissionBoardManagementService(commissionBoardRepository, commissionConfigRepository,
                guildProgressPort, commissionBoardService, commissionBoardRefillService);
        guildBoard = CommissionBoard.guildBoard(GUILD_BOARD_ID, GUILD_ID, BoardLevels.GUILD_START, USAGE_TAX_RATE);

        when(commissionBoardService.getBoard(GUILD_BOARD_ID)).thenReturn(guildBoard);
        when(commissionBoardService.canManageGuildBoard(MANAGER, GUILD_ID)).thenReturn(true);
        when(commissionConfigRepository.findUpgradeStep(UpgradeType.SLOTS, 2)).thenReturn(Optional.of(new UpgradeStep(1000, 3)));
        when(guildProgressPort.getGuildLevel(GUILD_ID)).thenReturn(3);
        when(guildProgressPort.withdrawFromBank(GUILD_ID, 1000)).thenReturn(true);
    }

    @Test
    void upgradeBoard_paysWithTheBankThenSavesAndFillsTheNewSlot() {
        // when
        CommissionBoard upgradedBoard = commissionBoardManagementService.upgradeBoard(MANAGER, GUILD_BOARD_ID, UpgradeType.SLOTS);

        // then
        assertEquals(2, upgradedBoard.getLevels().slotsPerType());
        verify(guildProgressPort).withdrawFromBank(GUILD_ID, 1000);
        verify(commissionBoardRepository).save(guildBoard);
        verify(commissionBoardRefillService).refillBoard(guildBoard);
    }

    @Test
    void upgradeBoard_throwsWhenThePlayerCannotManageTheBoard() {
        // when / then
        assertThrows(BoardManagementNotAllowedException.class,
                () -> commissionBoardManagementService.upgradeBoard(EXTERNAL, GUILD_BOARD_ID, UpgradeType.SLOTS));
        verify(guildProgressPort, never()).withdrawFromBank(any(), anyInt());
    }

    @Test
    void upgradeBoard_throwsOnTheSpawnBoard() {
        // given
        when(commissionBoardService.getBoard(SPAWN_BOARD_ID)).thenReturn(CommissionBoard.spawnBoard(SPAWN_BOARD_ID, BoardLevels.spawn(6)));

        // when / then
        assertThrows(BoardManagementNotAllowedException.class,
                () -> commissionBoardManagementService.upgradeBoard(MANAGER, SPAWN_BOARD_ID, UpgradeType.SLOTS));
    }

    @Test
    void upgradeBoard_throwsAtTheMaximumLevel() {
        // given
        when(commissionConfigRepository.findUpgradeStep(UpgradeType.SLOTS, 2)).thenReturn(Optional.empty());

        // when / then
        assertThrows(UpgradeMaxLevelReachedException.class,
                () -> commissionBoardManagementService.upgradeBoard(MANAGER, GUILD_BOARD_ID, UpgradeType.SLOTS));
    }

    @Test
    void upgradeBoard_throwsAndKeepsTheBankWhenTheGuildLevelIsTooLow() {
        // given
        when(guildProgressPort.getGuildLevel(GUILD_ID)).thenReturn(2);

        // when / then
        assertThrows(GuildLevelTooLowException.class,
                () -> commissionBoardManagementService.upgradeBoard(MANAGER, GUILD_BOARD_ID, UpgradeType.SLOTS));
        verify(guildProgressPort, never()).withdrawFromBank(any(), anyInt());
    }

    @Test
    void upgradeBoard_throwsAndKeepsTheBoardWhenTheBankCannotPay() {
        // given
        when(guildProgressPort.withdrawFromBank(GUILD_ID, 1000)).thenReturn(false);

        // when / then
        assertThrows(NotEnoughBankMoneyException.class,
                () -> commissionBoardManagementService.upgradeBoard(MANAGER, GUILD_BOARD_ID, UpgradeType.SLOTS));
        assertEquals(1, guildBoard.getLevels().slotsPerType());
        verify(commissionBoardRepository, never()).save(any());
    }

    @Test
    void changeUsageTax_savesTheNewRate() {
        // when
        commissionBoardManagementService.changeUsageTax(MANAGER, GUILD_BOARD_ID, 0.1);

        // then
        assertEquals(0.1, guildBoard.getUsageTaxRate());
        verify(commissionBoardRepository).save(guildBoard);
    }

    @Test
    void changeUsageTax_throwsAndSavesNothingOutsideTheAllowedRange() {
        // when / then
        assertThrows(InvalidUsageTaxRateException.class,
                () -> commissionBoardManagementService.changeUsageTax(MANAGER, GUILD_BOARD_ID, 0.6));
        verify(commissionBoardRepository, never()).save(any());
    }

    @Test
    void changeUsageTax_throwsAndKeepsTheRateWhenThePlayerCannotManageTheBoard() {
        // when / then
        assertThrows(BoardManagementNotAllowedException.class,
                () -> commissionBoardManagementService.changeUsageTax(EXTERNAL, GUILD_BOARD_ID, 0.1));
        assertEquals(USAGE_TAX_RATE, guildBoard.getUsageTaxRate());
    }
}
