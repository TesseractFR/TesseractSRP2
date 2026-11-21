package onl.tesseract.srp.commission.domain.port.userside;

import onl.tesseract.srp.commission.domain.model.board.CommissionBoardId;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardLevels;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardDeliveryRequirement;
import onl.tesseract.srp.commission.domain.model.board.GuildId;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardUpgradeStep;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardUpgradeType;
import onl.tesseract.srp.commission.domain.model.commission.CommissionType;
import onl.tesseract.srp.commission.domain.model.exception.CommissionBoardManagementNotAllowedException;
import onl.tesseract.srp.commission.domain.model.exception.CommissionBoardTierTooLowException;
import onl.tesseract.srp.commission.domain.model.exception.InvalidUsageTaxRateException;
import onl.tesseract.srp.commission.domain.model.exception.NotEnoughBankMoneyException;
import onl.tesseract.srp.commission.domain.model.exception.NotEnoughDeliveredItemsException;
import onl.tesseract.srp.commission.domain.model.exception.CommissionBoardUpgradeMaxLevelReachedException;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionBoardRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionConfigRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionRepository;
import onl.tesseract.srp.commission.domain.port.serverside.GuildBankPort;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
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

    private static final CommissionBoardId GUILD_BOARD_ID = new CommissionBoardId(1);
    private static final CommissionBoardId SPAWN_BOARD_ID = new CommissionBoardId(2);
    private static final GuildId GUILD_ID = new GuildId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final PlayerId MANAGER = new PlayerId(UUID.fromString("00000000-0000-0000-0000-000000000010"));
    private static final PlayerId EXTERNAL = new PlayerId(UUID.fromString("00000000-0000-0000-0000-000000000020"));
    private static final double USAGE_TAX_RATE = 0.25;
    private static final CommissionBoardDeliveryRequirement GOOD_HARVEST = new CommissionBoardDeliveryRequirement(CommissionType.HARVEST, Quality.GOOD, 100);

    private CommissionBoardRepository commissionBoardRepository;
    private CommissionConfigRepository commissionConfigRepository;
    private CommissionRepository commissionRepository;
    private GuildBankPort guildBankPort;
    private CommissionBoardService commissionBoardService;
    private CommissionBoardRefillService commissionBoardRefillService;
    private CommissionBoardManagementService commissionBoardManagementService;
    private CommissionBoard guildBoard;

    @BeforeEach
    void setUp() {
        commissionBoardRepository = mock(CommissionBoardRepository.class);
        commissionConfigRepository = mock(CommissionConfigRepository.class);
        commissionRepository = mock(CommissionRepository.class);
        guildBankPort = mock(GuildBankPort.class);
        commissionBoardService = mock(CommissionBoardService.class);
        commissionBoardRefillService = mock(CommissionBoardRefillService.class);
        commissionBoardManagementService = new CommissionBoardManagementService(commissionBoardRepository, commissionConfigRepository,
                commissionRepository, guildBankPort, commissionBoardService, commissionBoardRefillService);
        guildBoard = CommissionBoard.guildBoard(GUILD_BOARD_ID, GUILD_ID, CommissionBoardLevels.GUILD_BOARD_START, USAGE_TAX_RATE);

        when(commissionBoardService.getBoard(GUILD_BOARD_ID)).thenReturn(guildBoard);
        when(commissionBoardService.canManageBoard(MANAGER, guildBoard)).thenReturn(true);
        when(commissionConfigRepository.findUpgradeStep(CommissionBoardUpgradeType.HARVEST_SLOTS, 2))
                .thenReturn(Optional.of(new CommissionBoardUpgradeStep(1000, 1, List.of())));
        when(commissionConfigRepository.findUpgradeStep(CommissionBoardUpgradeType.BOARD_TIER, 2))
                .thenReturn(Optional.of(new CommissionBoardUpgradeStep(1000, 1, List.of(GOOD_HARVEST))));
        when(guildBankPort.withdrawFromBank(GUILD_ID, 1000)).thenReturn(true);
    }

    @Test
    void upgradeBoard_paysWithTheBankThenSavesAndFillsTheNewSlot() {
        // when
        CommissionBoard upgradedBoard = commissionBoardManagementService.upgradeBoard(MANAGER, GUILD_BOARD_ID, CommissionBoardUpgradeType.HARVEST_SLOTS);

        // then
        assertEquals(2, upgradedBoard.getLevels().harvestSlots());
        assertEquals(1, upgradedBoard.getLevels().craftSlots());
        verify(guildBankPort).withdrawFromBank(GUILD_ID, 1000);
        verify(commissionBoardRepository).save(guildBoard);
        verify(commissionBoardRefillService).refillBoard(guildBoard);
    }

    @Test
    void upgradeBoard_throwsWhenThePlayerCannotManageTheBoard() {
        // when / then
        assertThrows(CommissionBoardManagementNotAllowedException.class,
                () -> commissionBoardManagementService.upgradeBoard(EXTERNAL, GUILD_BOARD_ID, CommissionBoardUpgradeType.HARVEST_SLOTS));
        verify(guildBankPort, never()).withdrawFromBank(any(), anyInt());
    }

    @Test
    void upgradeBoard_throwsOnTheSpawnBoard() {
        // given
        when(commissionBoardService.getBoard(SPAWN_BOARD_ID)).thenReturn(CommissionBoard.spawnBoard(SPAWN_BOARD_ID, CommissionBoardLevels.forSpawnBoard(6)));

        // when / then
        assertThrows(CommissionBoardManagementNotAllowedException.class,
                () -> commissionBoardManagementService.upgradeBoard(MANAGER, SPAWN_BOARD_ID, CommissionBoardUpgradeType.HARVEST_SLOTS));
    }

    @Test
    void upgradeBoard_throwsAtTheMaximumLevel() {
        // given
        when(commissionConfigRepository.findUpgradeStep(CommissionBoardUpgradeType.HARVEST_SLOTS, 2)).thenReturn(Optional.empty());

        // when / then
        assertThrows(CommissionBoardUpgradeMaxLevelReachedException.class,
                () -> commissionBoardManagementService.upgradeBoard(MANAGER, GUILD_BOARD_ID, CommissionBoardUpgradeType.HARVEST_SLOTS));
    }

    @Test
    void upgradeBoard_throwsAndKeepsTheBankWhenTheBoardTierIsTooLow() {
        // given
        when(commissionConfigRepository.findUpgradeStep(CommissionBoardUpgradeType.HARVEST_SLOTS, 2))
                .thenReturn(Optional.of(new CommissionBoardUpgradeStep(1000, 2, List.of())));

        // when / then
        assertThrows(CommissionBoardTierTooLowException.class,
                () -> commissionBoardManagementService.upgradeBoard(MANAGER, GUILD_BOARD_ID, CommissionBoardUpgradeType.HARVEST_SLOTS));
        verify(guildBankPort, never()).withdrawFromBank(any(), anyInt());
    }

    @Test
    void upgradeBoard_throwsAndKeepsTheBankWhenTheBoardHasNotReceivedEnoughDeliveries() {
        // given
        when(commissionRepository.countDeliveredItems(GUILD_BOARD_ID, CommissionType.HARVEST, Quality.GOOD)).thenReturn(99);

        // when / then
        assertThrows(NotEnoughDeliveredItemsException.class,
                () -> commissionBoardManagementService.upgradeBoard(MANAGER, GUILD_BOARD_ID, CommissionBoardUpgradeType.BOARD_TIER));
        verify(guildBankPort, never()).withdrawFromBank(any(), anyInt());
    }

    @Test
    void upgradeBoard_countsDeliveriesOfHigherQualitiesTowardsTheBoardTier() {
        // given : 60 items livrés en qualité bonne et 40 en très bonne, pour 100 demandés en bonne ou mieux
        when(commissionRepository.countDeliveredItems(GUILD_BOARD_ID, CommissionType.HARVEST, Quality.GOOD)).thenReturn(60);
        when(commissionRepository.countDeliveredItems(GUILD_BOARD_ID, CommissionType.HARVEST, Quality.VERY_GOOD)).thenReturn(40);
        when(commissionRepository.countDeliveredItems(GUILD_BOARD_ID, CommissionType.HARVEST, Quality.NORMAL)).thenReturn(500);

        // when
        CommissionBoard upgradedBoard = commissionBoardManagementService.upgradeBoard(MANAGER, GUILD_BOARD_ID, CommissionBoardUpgradeType.BOARD_TIER);

        // then
        assertEquals(2, upgradedBoard.getLevels().boardTier());
        assertEquals(100, commissionBoardManagementService.countDeliveredItems(guildBoard, GOOD_HARVEST));
    }

    @Test
    void upgradeBoard_throwsAndKeepsTheBoardWhenTheBankCannotPay() {
        // given
        when(guildBankPort.withdrawFromBank(GUILD_ID, 1000)).thenReturn(false);

        // when / then
        assertThrows(NotEnoughBankMoneyException.class,
                () -> commissionBoardManagementService.upgradeBoard(MANAGER, GUILD_BOARD_ID, CommissionBoardUpgradeType.HARVEST_SLOTS));
        assertEquals(1, guildBoard.getLevels().harvestSlots());
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
        assertThrows(CommissionBoardManagementNotAllowedException.class,
                () -> commissionBoardManagementService.changeUsageTax(EXTERNAL, GUILD_BOARD_ID, 0.1));
        assertEquals(USAGE_TAX_RATE, guildBoard.getUsageTaxRate());
    }
    @Test
    void getMaxLevelAtBoardTier_stopsAtTheFirstStepThatNeedsAHigherBoardTier() {
        // given : le palier 2 est accessible au tier 1, le palier 3 demande le tier 2
        when(commissionConfigRepository.findUpgradeStep(CommissionBoardUpgradeType.HARVEST_SLOTS, 3))
                .thenReturn(Optional.of(new CommissionBoardUpgradeStep(2000, 2, List.of())));

        // when
        int maxLevel = commissionBoardManagementService.getMaxLevelAtBoardTier(guildBoard, CommissionBoardUpgradeType.HARVEST_SLOTS);

        // then
        assertEquals(2, maxLevel);
    }
}
