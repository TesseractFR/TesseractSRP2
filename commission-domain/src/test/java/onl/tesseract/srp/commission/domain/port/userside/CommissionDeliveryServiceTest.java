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
import onl.tesseract.srp.commission.domain.model.commission.CommissionDelivery;
import onl.tesseract.srp.commission.domain.model.exception.CommissionBoardPartAccessDeniedException;
import onl.tesseract.srp.commission.domain.model.exception.CommissionNotAvailableException;
import onl.tesseract.srp.commission.domain.model.exception.ItemQualityTooLowException;
import onl.tesseract.srp.commission.domain.model.exception.NoItemToDeliverException;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionConfigRepository;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionRepository;
import onl.tesseract.srp.commission.domain.port.serverside.GuildIncomePort;
import onl.tesseract.srp.commission.domain.port.serverside.PlayerMaterialPort;
import onl.tesseract.srp.commission.domain.port.serverside.PlayerPaymentPort;
import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommissionDeliveryServiceTest {

    private static final CommissionBoardId GUILD_BOARD_ID = new CommissionBoardId(1);
    private static final CommissionBoardId SPAWN_BOARD_ID = new CommissionBoardId(2);
    private static final GuildId GUILD_ID = new GuildId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final PlayerId MEMBER = new PlayerId(UUID.fromString("00000000-0000-0000-0000-000000000010"));
    private static final PlayerId EXTERNAL = new PlayerId(UUID.fromString("00000000-0000-0000-0000-000000000020"));
    private static final CommissionId COMMISSION_ID = new CommissionId(100);
    private static final MaterialName WOOD = new MaterialName("BOIS");

    private static Commission commission(CommissionBoardId boardId, CommissionBoardPart part, int deliveredQuantity) {
        return new Commission(COMMISSION_ID, boardId, part, CommissionType.HARVEST, WOOD, 10, deliveredQuantity,
                Quality.NORMAL, MoneyValue.of(100));
    }

    private CommissionRepository commissionRepository;
    private PlayerMaterialPort playerMaterialPort;
    private PlayerPaymentPort playerPaymentPort;
    private GuildIncomePort guildIncomePort;
    private CommissionBoardService commissionBoardService;
    private CommissionBoardAccessService commissionBoardAccessService;
    private CommissionBoardRefillService commissionBoardRefillService;
    private CommissionDeliveryService commissionDeliveryService;
    private CommissionBoard guildBoard;

    @BeforeEach
    void setUp() {
        commissionRepository = mock(CommissionRepository.class);
        playerMaterialPort = mock(PlayerMaterialPort.class);
        playerPaymentPort = mock(PlayerPaymentPort.class);
        guildIncomePort = mock(GuildIncomePort.class);
        CommissionConfigRepository commissionConfigRepository = mock(CommissionConfigRepository.class);
        commissionBoardService = mock(CommissionBoardService.class);
        commissionBoardAccessService = mock(CommissionBoardAccessService.class);
        commissionBoardRefillService = mock(CommissionBoardRefillService.class);
        commissionDeliveryService = new CommissionDeliveryService(commissionRepository, playerMaterialPort, playerPaymentPort,
                guildIncomePort, commissionConfigRepository, commissionBoardService, commissionBoardAccessService,
                commissionBoardRefillService);
        guildBoard = CommissionBoard.guildBoard(GUILD_BOARD_ID, GUILD_ID, CommissionBoardLevels.GUILD_BOARD_START, new UsageTaxRate(25));

        when(commissionBoardService.getBoard(GUILD_BOARD_ID)).thenReturn(guildBoard);
        when(commissionBoardAccessService.isGuildMember(MEMBER, guildBoard)).thenReturn(true);
        when(commissionBoardAccessService.canAccessPart(any(), any(), any())).thenReturn(true);
        when(commissionConfigRepository.getGuildXpPerLys()).thenReturn(0.5);
    }

    private void givenCommission(Commission commission) {
        when(commissionRepository.findById(COMMISSION_ID)).thenReturn(Optional.of(commission));
    }

    private void givenOwnedItems(PlayerId player, int ownedQuantity) {
        when(playerMaterialPort.countAtLeast(player, WOOD, Quality.NORMAL)).thenReturn(ownedQuantity);
    }

    @Test
    void deliver_recordsTheDeliveryThenRemovesTheItemsThenPays() {
        // given
        givenCommission(commission(GUILD_BOARD_ID, CommissionBoardPart.PUBLIC, 0));
        givenOwnedItems(MEMBER, 5);

        // when
        CommissionDelivery delivery = commissionDeliveryService.deliver(MEMBER, COMMISSION_ID);

        // then
        InOrder deliveryOrder = inOrder(commissionRepository, playerMaterialPort, playerPaymentPort, guildIncomePort);
        deliveryOrder.verify(commissionRepository).recordDelivery(delivery);
        deliveryOrder.verify(playerMaterialPort).removeAtLeast(MEMBER, WOOD, Quality.NORMAL, 5);
        deliveryOrder.verify(playerPaymentPort).pay(MEMBER, 50);
        deliveryOrder.verify(guildIncomePort).addCommissionIncome(GUILD_ID, 0, delivery.guildXp());
        assertEquals(50, delivery.playerEarnings());
    }

    @Test
    void deliver_paysTheTaxOfAnExternalPlayerToTheGuild() {
        // given : 25 % de taxe sur 50 lys
        givenCommission(commission(GUILD_BOARD_ID, CommissionBoardPart.PUBLIC, 0));
        givenOwnedItems(EXTERNAL, 5);

        // when
        commissionDeliveryService.deliver(EXTERNAL, COMMISSION_ID);

        // then
        verify(playerPaymentPort).pay(EXTERNAL, 38);
        verify(guildIncomePort).addCommissionIncome(GUILD_ID, 12, 25);
    }

    @Test
    void deliver_paysNoGuildOnTheSpawnBoard() {
        // given
        CommissionBoard spawnBoard = CommissionBoard.spawnBoard(SPAWN_BOARD_ID, 6);
        when(commissionBoardService.getBoard(SPAWN_BOARD_ID)).thenReturn(spawnBoard);
        givenCommission(commission(SPAWN_BOARD_ID, CommissionBoardPart.PUBLIC, 0));
        givenOwnedItems(EXTERNAL, 5);

        // when
        commissionDeliveryService.deliver(EXTERNAL, COMMISSION_ID);

        // then
        verify(playerPaymentPort).pay(EXTERNAL, 50);
        verify(guildIncomePort, never()).addCommissionIncome(any(), anyInt(), anyInt());
    }

    @Test
    void deliver_removesAtMostTheRemainingQuantityAndRefillsTheBoard() {
        // given
        givenCommission(commission(GUILD_BOARD_ID, CommissionBoardPart.PUBLIC, 8));
        givenOwnedItems(MEMBER, 64);

        // when
        CommissionDelivery delivery = commissionDeliveryService.deliver(MEMBER, COMMISSION_ID);

        // then
        assertEquals(2, delivery.itemCount());
        assertTrue(delivery.completesCommission());
        verify(commissionBoardRefillService).refillBoard(guildBoard);
    }

    @Test
    void computeDelivery_taxesAnExternalPlayerButNotAMember() {
        // given
        Commission commission = commission(GUILD_BOARD_ID, CommissionBoardPart.PUBLIC, 0);
        givenOwnedItems(EXTERNAL, 10);
        givenOwnedItems(MEMBER, 10);

        // when
        CommissionDelivery externalDelivery = commissionDeliveryService.computeDelivery(EXTERNAL, guildBoard, commission);
        CommissionDelivery memberDelivery = commissionDeliveryService.computeDelivery(MEMBER, guildBoard, commission);

        // then
        assertEquals(25, externalDelivery.guildTax());
        assertEquals(75, externalDelivery.playerEarnings());
        assertEquals(0, memberDelivery.guildTax());
        assertEquals(100, memberDelivery.playerEarnings());
    }

    @Test
    void computeDelivery_givesTheGuildXpInProportionToTheDeliveredQuantity() {
        // given : 100 lys à 0,5 XP par lys, soit 50 XP pour toute la commission
        givenOwnedItems(MEMBER, 4);

        // when
        CommissionDelivery delivery = commissionDeliveryService.computeDelivery(MEMBER, guildBoard, commission(GUILD_BOARD_ID, CommissionBoardPart.PUBLIC, 0));

        // then
        assertEquals(20, delivery.guildXp());
    }

    @Test
    void computeDelivery_givesNoGuildXpAndNoTaxOnTheSpawnBoard() {
        // given
        CommissionBoard spawnBoard = CommissionBoard.spawnBoard(SPAWN_BOARD_ID, 6);
        givenOwnedItems(EXTERNAL, 10);

        // when
        CommissionDelivery delivery = commissionDeliveryService.computeDelivery(EXTERNAL, spawnBoard, commission(SPAWN_BOARD_ID, CommissionBoardPart.PUBLIC, 0));

        // then
        assertEquals(0, delivery.guildXp());
        assertEquals(0, delivery.guildTax());
    }

    @Test
    void computeTotalGuildXp_isTheRewardTimesTheRateOnAGuildBoardAndZeroAtTheSpawn() {
        // given
        CommissionBoard spawnBoard = CommissionBoard.spawnBoard(SPAWN_BOARD_ID, 6);

        // when / then : 100 lys à 0,5 XP par lys
        assertEquals(50, commissionDeliveryService.computeTotalGuildXp(guildBoard, commission(GUILD_BOARD_ID, CommissionBoardPart.PUBLIC, 0)));
        assertEquals(0, commissionDeliveryService.computeTotalGuildXp(spawnBoard, commission(SPAWN_BOARD_ID, CommissionBoardPart.PUBLIC, 0)));
    }

    @Test
    void deliver_throwsWhenTheCommissionIsCompleted() {
        // given
        givenCommission(commission(GUILD_BOARD_ID, CommissionBoardPart.PUBLIC, 10));

        // when / then
        assertThrows(CommissionNotAvailableException.class, () -> commissionDeliveryService.deliver(MEMBER, COMMISSION_ID));
    }

    @Test
    void deliver_throwsWhenThePlayerCannotAccessThePart() {
        // given
        givenCommission(commission(GUILD_BOARD_ID, CommissionBoardPart.GUILD, 0));
        when(commissionBoardAccessService.canAccessPart(EXTERNAL, guildBoard, CommissionBoardPart.GUILD)).thenReturn(false);

        // when / then
        assertThrows(CommissionBoardPartAccessDeniedException.class, () -> commissionDeliveryService.deliver(EXTERNAL, COMMISSION_ID));
        verify(commissionRepository, never()).recordDelivery(any());
    }

    @Test
    void deliver_throwsWhenThePlayerHasTheItemOnlyBelowTheMinimalQuality() {
        // given
        givenCommission(commission(GUILD_BOARD_ID, CommissionBoardPart.PUBLIC, 0));
        givenOwnedItems(MEMBER, 0);
        when(playerMaterialPort.countAtLeast(MEMBER, WOOD, Quality.POOR)).thenReturn(3);

        // when / then
        assertThrows(ItemQualityTooLowException.class, () -> commissionDeliveryService.deliver(MEMBER, COMMISSION_ID));
        verify(commissionRepository, never()).recordDelivery(any());
    }

    @Test
    void deliver_throwsWhenThePlayerHasNoItem() {
        // given
        givenCommission(commission(GUILD_BOARD_ID, CommissionBoardPart.PUBLIC, 0));
        givenOwnedItems(MEMBER, 0);

        // when / then
        assertThrows(NoItemToDeliverException.class, () -> commissionDeliveryService.deliver(MEMBER, COMMISSION_ID));
    }
}
