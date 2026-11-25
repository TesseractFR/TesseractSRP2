package onl.tesseract.srp.commission.domain.port.userside;

import onl.tesseract.srp.commission.domain.model.board.CommissionBoardDeliveryProgress;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardUpgradeConditions;
import onl.tesseract.srp.commission.domain.model.board.UsageTaxRate;
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
import onl.tesseract.srp.commission.domain.port.serverside.GuildMembershipPort;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    private static final UsageTaxRate USAGE_TAX_RATE = new UsageTaxRate(25);
    private static final CommissionBoardDeliveryRequirement GOOD_HARVEST = new CommissionBoardDeliveryRequirement(CommissionType.HARVEST, Quality.GOOD, 100);

    private CommissionBoardRepository commissionBoardRepository;
    private CommissionConfigRepository commissionConfigRepository;
    private CommissionRepository commissionRepository;
    private GuildBankPort guildBankPort;
    private CommissionBoardService commissionBoardService;
    private GuildMembershipPort guildMembershipPort;
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
        guildMembershipPort = mock(GuildMembershipPort.class);
        commissionBoardRefillService = mock(CommissionBoardRefillService.class);
        commissionBoardManagementService = new CommissionBoardManagementService(commissionBoardRepository, commissionConfigRepository,
                commissionRepository, guildBankPort, guildMembershipPort, commissionBoardService, commissionBoardRefillService);
        guildBoard = CommissionBoard.guildBoard(GUILD_BOARD_ID, GUILD_ID, CommissionBoardLevels.GUILD_BOARD_START, USAGE_TAX_RATE);

        when(commissionBoardService.getBoard(GUILD_BOARD_ID)).thenReturn(guildBoard);
        when(guildMembershipPort.canManageBoard(GUILD_ID, MANAGER)).thenReturn(true);
        when(commissionConfigRepository.findUpgradeStep(CommissionBoardUpgradeType.HARVEST_SLOTS, 2))
                .thenReturn(Optional.of(new CommissionBoardUpgradeStep(1000, 1, List.of())));
        when(commissionConfigRepository.findUpgradeStep(CommissionBoardUpgradeType.BOARD_TIER, 2))
                .thenReturn(Optional.of(new CommissionBoardUpgradeStep(1000, 1, List.of(GOOD_HARVEST))));
        when(guildBankPort.withdrawFromBank(GUILD_ID, 1000)).thenReturn(true);
    }

    @Test
    void canManageBoard_isAlwaysFalseOnTheSpawnBoard() {
        // given
        CommissionBoard spawnBoard = CommissionBoard.spawnBoard(SPAWN_BOARD_ID, 6);

        // when / then
        assertFalse(commissionBoardManagementService.canManageBoard(MANAGER, spawnBoard));
        verify(guildMembershipPort, never()).canManageBoard(any(), any());
    }

    @Test
    void upgradeBoard_paysWithTheBankThenSavesAndFillsTheNewSlot() {
        // when
        commissionBoardManagementService.upgradeBoard(MANAGER, GUILD_BOARD_ID, CommissionBoardUpgradeType.HARVEST_SLOTS);

        // then
        assertEquals(2, guildBoard.getLevels().harvestSlots());
        assertEquals(1, guildBoard.getLevels().craftSlots());
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
        when(commissionBoardService.getBoard(SPAWN_BOARD_ID)).thenReturn(CommissionBoard.spawnBoard(SPAWN_BOARD_ID, 6));

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
        when(commissionRepository.countDeliveredItemsAtLeast(GUILD_BOARD_ID, CommissionType.HARVEST, Quality.GOOD)).thenReturn(99);

        // when / then
        assertThrows(NotEnoughDeliveredItemsException.class,
                () -> commissionBoardManagementService.upgradeBoard(MANAGER, GUILD_BOARD_ID, CommissionBoardUpgradeType.BOARD_TIER));
        verify(guildBankPort, never()).withdrawFromBank(any(), anyInt());
    }

    @Test
    void upgradeBoard_raisesTheBoardTierOnceTheDeliveriesAreMet() {
        // given : 100 items livrés en qualité bonne ou mieux, pour 100 demandés
        when(commissionRepository.countDeliveredItemsAtLeast(GUILD_BOARD_ID, CommissionType.HARVEST, Quality.GOOD)).thenReturn(100);

        // when
        commissionBoardManagementService.upgradeBoard(MANAGER, GUILD_BOARD_ID, CommissionBoardUpgradeType.BOARD_TIER);

        // then
        assertEquals(2, guildBoard.getLevels().boardTier());
    }

    @Test
    void findNextUpgradeConditions_reportsEachConditionOfTheNextStep() {
        // given : 1 500 lys en banque pour un palier à 1 000, 40 items livrés sur 100
        when(guildBankPort.getBankBalance(GUILD_ID)).thenReturn(1500);
        when(commissionRepository.countDeliveredItemsAtLeast(GUILD_BOARD_ID, CommissionType.HARVEST, Quality.GOOD)).thenReturn(40);

        // when
        CommissionBoardUpgradeConditions conditions = commissionBoardManagementService
                .findNextUpgradeConditions(guildBoard, CommissionBoardUpgradeType.BOARD_TIER).orElseThrow();

        // then
        assertTrue(conditions.hasEnoughMoney());
        assertTrue(conditions.hasBoardTier());
        assertEquals(List.of(new CommissionBoardDeliveryProgress(GOOD_HARVEST, 40)), conditions.deliveryProgress());
        assertFalse(conditions.areAllMet());
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
        commissionBoardManagementService.changeUsageTax(MANAGER, GUILD_BOARD_ID, 10);

        // then
        assertEquals(new UsageTaxRate(10), guildBoard.getUsageTaxRate());
        verify(commissionBoardRepository).save(guildBoard);
    }

    @Test
    void changeUsageTax_throwsAndSavesNothingOutsideTheAllowedRange() {
        // when / then
        assertThrows(InvalidUsageTaxRateException.class,
                () -> commissionBoardManagementService.changeUsageTax(MANAGER, GUILD_BOARD_ID, 60));
        verify(commissionBoardRepository, never()).save(any());
    }

    @Test
    void changeUsageTax_throwsAndKeepsTheRateWhenThePlayerCannotManageTheBoard() {
        // when / then
        assertThrows(CommissionBoardManagementNotAllowedException.class,
                () -> commissionBoardManagementService.changeUsageTax(EXTERNAL, GUILD_BOARD_ID, 10));
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
