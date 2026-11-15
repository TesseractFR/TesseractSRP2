package onl.tesseract.srp.contract.domain.port.userside;

import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.contract.ContractClient;
import onl.tesseract.srp.contract.domain.model.contract.ContractDraft;
import onl.tesseract.srp.contract.domain.model.contract.ContractId;
import onl.tesseract.srp.contract.domain.model.contract.ContractType;
import onl.tesseract.srp.contract.domain.model.exception.ContractExpiredException;
import onl.tesseract.srp.contract.domain.model.exception.ContractNotFoundException;
import onl.tesseract.srp.contract.domain.model.exception.ContractScrollNotOwnedException;
import onl.tesseract.srp.contract.domain.model.exception.InvalidTicketAmountException;
import onl.tesseract.srp.contract.domain.model.exception.MissingContractItemsException;
import onl.tesseract.srp.contract.domain.model.exception.NoContractAvailableException;
import onl.tesseract.srp.contract.domain.model.exception.NoContractTicketLeftException;
import onl.tesseract.srp.contract.domain.model.exception.PaymentFailedException;
import onl.tesseract.srp.contract.domain.model.exception.ScrollInventoryFullException;
import onl.tesseract.srp.contract.domain.model.exception.WrongContractClientException;
import onl.tesseract.srp.contract.domain.port.serverside.ContractRepository;
import onl.tesseract.srp.contract.domain.port.serverside.ContractScrollPort;
import onl.tesseract.srp.contract.domain.port.serverside.ContractTicketRepository;
import onl.tesseract.srp.contract.domain.port.serverside.DailyTicketQuotaPort;
import onl.tesseract.srp.contract.domain.port.serverside.JobMoneyBonusPort;
import onl.tesseract.srp.contract.domain.port.serverside.PaymentPort;
import onl.tesseract.srp.contract.domain.port.serverside.PlayerMaterialPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContractServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-15T12:00:00Z");
    private static final PlayerId PLAYER = new PlayerId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final PlayerId TARGET = new PlayerId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
    private static final ContractId CONTRACT_ID = new ContractId(42);
    private static final ContractClient FARMER = new ContractClient("farmer");
    private static final MaterialName WHEAT = new MaterialName("wheat");

    private ContractRepository contractRepository;
    private ContractGenerator contractGenerator;
    private ContractScrollPort contractScrollPort;
    private PlayerMaterialPort playerMaterialPort;
    private ContractTicketRepository contractTicketRepository;
    private DailyTicketQuotaPort dailyTicketQuotaPort;
    private JobMoneyBonusPort jobMoneyBonusPort;
    private PaymentPort paymentPort;
    private ContractService contractService;

    @BeforeEach
    void setUp() {
        contractRepository = mock(ContractRepository.class);
        contractGenerator = mock(ContractGenerator.class);
        contractScrollPort = mock(ContractScrollPort.class);
        playerMaterialPort = mock(PlayerMaterialPort.class);
        contractTicketRepository = mock(ContractTicketRepository.class);
        dailyTicketQuotaPort = mock(DailyTicketQuotaPort.class);
        jobMoneyBonusPort = mock(JobMoneyBonusPort.class);
        paymentPort = mock(PaymentPort.class);
        contractService = new ContractService(contractRepository, contractGenerator, contractScrollPort,
                playerMaterialPort, contractTicketRepository, dailyTicketQuotaPort, jobMoneyBonusPort, paymentPort,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private static Contract contract(ContractType type, Instant expiresAt) {
        return new Contract(CONTRACT_ID, type, FARMER, WHEAT, 10, Quality.POOR, MoneyValue.of(100), expiresAt);
    }

    private static Contract validHarvestContract() {
        return contract(ContractType.HARVEST, NOW.plusSeconds(3600));
    }

    private static ContractDraft draft() {
        return new ContractDraft(ContractType.HARVEST, FARMER, WHEAT, 10, Quality.POOR, MoneyValue.of(100),
                NOW.plusSeconds(3600));
    }

    private static void assertMoney(int expected, MoneyValue actual) {
        assertEquals(0, BigDecimal.valueOf(expected).compareTo(actual.value()),
                () -> "attendu " + expected + ", obtenu " + actual.value());
    }

    @Nested
    class BuyScroll {

        @BeforeEach
        void givenPlayerCanBuy() {
            when(contractTicketRepository.findTickets(PLAYER)).thenReturn(OptionalInt.of(3));
            when(contractScrollPort.canReceiveScroll(PLAYER)).thenReturn(true);
            when(contractGenerator.generate()).thenReturn(Optional.of(draft()));
            when(contractRepository.create(any())).thenReturn(validHarvestContract());
            when(contractScrollPort.giveScroll(any(), any(), anyBoolean())).thenReturn(true);
        }

        @Test
        void buyScroll_returnsCreatedContractAndSpendsOneTicket() {
            // when
            Contract contract = contractService.buyScroll(PLAYER);

            // then
            assertEquals(CONTRACT_ID, contract.id());
            verify(contractTicketRepository).saveTickets(PLAYER, 2);
            verify(contractScrollPort).giveScroll(PLAYER, contract, false);
        }

        @Test
        void buyScroll_throwsWhenPlayerHasNoTicketLeft() {
            // given
            when(contractTicketRepository.findTickets(PLAYER)).thenReturn(OptionalInt.of(0));

            // when / then
            assertThrows(NoContractTicketLeftException.class, () -> contractService.buyScroll(PLAYER));
            verify(contractRepository, never()).create(any());
        }

        @Test
        void buyScroll_usesDailyQuotaWhenPlayerHasNoSavedBalance() {
            // given
            when(contractTicketRepository.findTickets(PLAYER)).thenReturn(OptionalInt.empty());
            when(dailyTicketQuotaPort.getDailyQuota(PLAYER)).thenReturn(5);

            // when
            contractService.buyScroll(PLAYER);

            // then
            verify(contractTicketRepository).saveTickets(PLAYER, 4);
        }

        @Test
        void buyScroll_throwsWhenInventoryCannotReceiveScroll() {
            // given
            when(contractScrollPort.canReceiveScroll(PLAYER)).thenReturn(false);

            // when / then
            assertThrows(ScrollInventoryFullException.class, () -> contractService.buyScroll(PLAYER));
            verify(contractTicketRepository, never()).saveTickets(any(), anyInt());
        }

        @Test
        void buyScroll_throwsWhenCatalogIsEmpty() {
            // given
            when(contractGenerator.generate()).thenReturn(Optional.empty());

            // when / then
            assertThrows(NoContractAvailableException.class, () -> contractService.buyScroll(PLAYER));
            verify(contractTicketRepository, never()).saveTickets(any(), anyInt());
        }

        @Test
        void buyScroll_refundsTicketWhenSavingFails() {
            // given
            IllegalStateException databaseFailure = new IllegalStateException("base indisponible");
            when(contractRepository.create(any())).thenThrow(databaseFailure);
            when(contractTicketRepository.findTickets(PLAYER)).thenReturn(OptionalInt.of(3), OptionalInt.of(3),
                    OptionalInt.of(2));

            // when
            IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> contractService.buyScroll(PLAYER));

            // then
            assertSame(databaseFailure, thrown);
            verify(contractTicketRepository).saveTickets(PLAYER, 2);
            verify(contractTicketRepository).saveTickets(PLAYER, 3);
        }

        @Test
        void buyScroll_cancelsPurchaseWhenScrollCannotBeGiven() {
            // given
            when(contractScrollPort.giveScroll(any(), any(), anyBoolean())).thenReturn(false);
            when(contractTicketRepository.findTickets(PLAYER)).thenReturn(OptionalInt.of(3), OptionalInt.of(3),
                    OptionalInt.of(2));

            // when / then
            assertThrows(ScrollInventoryFullException.class, () -> contractService.buyScroll(PLAYER));
            verify(contractRepository).delete(CONTRACT_ID);
            verify(contractTicketRepository).saveTickets(PLAYER, 3);
        }

        @Test
        void buyScroll_marksScrollWhenBuyerHasJobBonus() {
            // given
            when(jobMoneyBonusPort.getMoneyBonusRate(PLAYER, FARMER, WHEAT)).thenReturn(0.05);

            // when
            Contract contract = contractService.buyScroll(PLAYER);

            // then
            verify(contractScrollPort).giveScroll(PLAYER, contract, true);
        }
    }

    @Nested
    class Deliver {

        @BeforeEach
        void givenDeliverableContract() {
            when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.of(validHarvestContract()));
            when(contractScrollPort.hasScroll(PLAYER, CONTRACT_ID)).thenReturn(true);
            when(contractScrollPort.removeScroll(PLAYER, CONTRACT_ID)).thenReturn(true);
            when(playerMaterialPort.countAtLeast(PLAYER, WHEAT, Quality.POOR)).thenReturn(10);
        }

        @Test
        void deliver_removesScrollAndItemsDeletesContractAndPaysReward() {
            // when
            MoneyValue payment = contractService.deliver(PLAYER, CONTRACT_ID, FARMER);

            // then
            assertMoney(100, payment);
            verify(contractScrollPort).removeScroll(PLAYER, CONTRACT_ID);
            verify(playerMaterialPort).removeAtLeast(PLAYER, WHEAT, Quality.POOR, 10);
            verify(contractRepository).delete(CONTRACT_ID);
            verify(paymentPort).pay(PLAYER, payment);
        }

        @Test
        void deliver_addsDelivererJobBonusToHarvestReward() {
            // given
            when(jobMoneyBonusPort.getMoneyBonusRate(PLAYER, FARMER, WHEAT)).thenReturn(0.05);

            // when
            MoneyValue payment = contractService.deliver(PLAYER, CONTRACT_ID, FARMER);

            // then : 100 + 5 %
            assertMoney(105, payment);
        }

        @Test
        void deliver_throwsWhenContractDoesNotExist() {
            // given
            when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.empty());

            // when / then
            assertThrows(ContractNotFoundException.class, () -> contractService.deliver(PLAYER, CONTRACT_ID, FARMER));
            verify(paymentPort, never()).pay(any(), any());
        }

        @Test
        void deliver_throwsAndDeletesContractWhenExpired() {
            // given
            when(contractRepository.findById(CONTRACT_ID))
                    .thenReturn(Optional.of(contract(ContractType.HARVEST, NOW.minusSeconds(1))));

            // when / then
            assertThrows(ContractExpiredException.class, () -> contractService.deliver(PLAYER, CONTRACT_ID, FARMER));
            verify(contractRepository).delete(CONTRACT_ID);
            verify(contractScrollPort, never()).removeScroll(any(), any());
        }

        @Test
        void deliver_throwsWhenNpcIsNotTheContractClient() {
            // when
            WrongContractClientException thrown = assertThrows(WrongContractClientException.class,
                    () -> contractService.deliver(PLAYER, CONTRACT_ID, new ContractClient("miner")));

            // then
            assertEquals(FARMER, thrown.getExpectedClient());
            verify(contractScrollPort, never()).removeScroll(any(), any());
        }

        @Test
        void deliver_throwsWhenDelivererDoesNotOwnTheScroll() {
            // given
            when(contractScrollPort.hasScroll(PLAYER, CONTRACT_ID)).thenReturn(false);

            // when / then
            assertThrows(ContractScrollNotOwnedException.class, () -> contractService.deliver(PLAYER, CONTRACT_ID, FARMER));
            verify(playerMaterialPort, never()).removeAtLeast(any(), any(), any(), anyInt());
        }

        @Test
        void deliver_throwsWithMissingQuantityWhenItemsAreMissing() {
            // given
            when(playerMaterialPort.countAtLeast(PLAYER, WHEAT, Quality.POOR)).thenReturn(7);

            // when
            MissingContractItemsException thrown = assertThrows(MissingContractItemsException.class,
                    () -> contractService.deliver(PLAYER, CONTRACT_ID, FARMER));

            // then
            assertEquals(3, thrown.getMissingQuantity());
            verify(contractScrollPort, never()).removeScroll(any(), any());
        }

        @Test
        void deliver_throwsWithoutRemovingItemsWhenScrollRemovalFails() {
            // given
            when(contractScrollPort.removeScroll(PLAYER, CONTRACT_ID)).thenReturn(false);

            // when / then
            assertThrows(ContractScrollNotOwnedException.class, () -> contractService.deliver(PLAYER, CONTRACT_ID, FARMER));
            verify(playerMaterialPort, never()).removeAtLeast(any(), any(), any(), anyInt());
        }

        @Test
        void deliver_propagatesPaymentFailureAfterRemovingItems() {
            // given
            doThrow(new PaymentFailedException(PLAYER, MoneyValue.of(100))).when(paymentPort).pay(any(), any());

            // when / then
            assertThrows(PaymentFailedException.class, () -> contractService.deliver(PLAYER, CONTRACT_ID, FARMER));
            verify(playerMaterialPort).removeAtLeast(PLAYER, WHEAT, Quality.POOR, 10);
            verify(contractRepository).delete(CONTRACT_ID);
        }
    }

    @Nested
    class FindMoneyBonus {

        @Test
        void findMoneyBonus_returnsRoundedBonusForHarvest() {
            // given
            when(jobMoneyBonusPort.getMoneyBonusRate(PLAYER, FARMER, WHEAT)).thenReturn(0.044);

            // when
            Optional<MoneyValue> bonus = contractService.findMoneyBonus(PLAYER, validHarvestContract());

            // then : 100 × 4,4 % = 4,4 arrondi à 4
            assertMoney(4, bonus.orElseThrow());
        }

        @Test
        void findMoneyBonus_returnsEmptyForCraft() {
            // when / then
            assertTrue(contractService.findMoneyBonus(PLAYER, contract(ContractType.CRAFT, NOW.plusSeconds(60))).isEmpty());
        }

        @Test
        void findMoneyBonus_returnsEmptyWhenRateIsNegative() {
            // given
            when(jobMoneyBonusPort.getMoneyBonusRate(PLAYER, FARMER, WHEAT)).thenReturn(-0.5);

            // when / then
            assertTrue(contractService.findMoneyBonus(PLAYER, validHarvestContract()).isEmpty());
        }

        @Test
        void findMoneyBonus_returnsEmptyWhenBonusRoundsToZero() {
            // given
            when(jobMoneyBonusPort.getMoneyBonusRate(PLAYER, FARMER, WHEAT)).thenReturn(0.004);

            // when / then
            assertTrue(contractService.findMoneyBonus(PLAYER, validHarvestContract()).isEmpty());
        }
    }

    @Nested
    class Tickets {

        @Test
        void getTickets_returnsSavedBalance() {
            // given
            when(contractTicketRepository.findTickets(PLAYER)).thenReturn(OptionalInt.of(7));

            // when / then
            assertEquals(7, contractService.getTickets(PLAYER));
        }

        @Test
        void getTickets_returnsDailyQuotaForNewPlayer() {
            // given
            when(contractTicketRepository.findTickets(PLAYER)).thenReturn(OptionalInt.empty());
            when(dailyTicketQuotaPort.getDailyQuota(PLAYER)).thenReturn(4);

            // when / then
            assertEquals(4, contractService.getTickets(PLAYER));
        }

        @Test
        void giveTickets_addsAmountToBalanceWithoutCap() {
            // given
            when(contractTicketRepository.findTickets(TARGET)).thenReturn(OptionalInt.of(50));

            // when
            contractService.giveTickets(TARGET, 100);

            // then
            verify(contractTicketRepository).saveTickets(TARGET, 150);
        }

        @Test
        void giveTickets_throwsWhenAmountIsNotStrictlyPositive() {
            // when
            InvalidTicketAmountException thrown = assertThrows(InvalidTicketAmountException.class,
                    () -> contractService.giveTickets(TARGET, 0));

            // then
            assertEquals(0, thrown.getAmount());
            verify(contractTicketRepository, never()).saveTickets(any(), anyInt());
        }

        @Test
        void renewDaily_resetsBalanceToQuota() {
            // given
            when(dailyTicketQuotaPort.getDailyQuota(PLAYER)).thenReturn(3);
            when(contractTicketRepository.findTickets(PLAYER)).thenReturn(OptionalInt.of(0));

            // when
            contractService.renewDaily(PLAYER);

            // then
            verify(contractTicketRepository).saveTickets(PLAYER, 3);
        }

        @Test
        void renewDaily_doesNothingWhenBalanceAlreadyEqualsQuota() {
            // given
            when(dailyTicketQuotaPort.getDailyQuota(PLAYER)).thenReturn(3);
            when(contractTicketRepository.findTickets(PLAYER)).thenReturn(OptionalInt.of(3));

            // when
            contractService.renewDaily(PLAYER);

            // then
            verify(contractTicketRepository, never()).saveTickets(any(), anyInt());
        }
    }

    @Nested
    class Expiration {

        @Test
        void purgeExpired_deletesContractsExpiredAtCurrentInstant() {
            // given
            when(contractRepository.deleteExpired(NOW)).thenReturn(4);

            // when / then
            assertEquals(4, contractService.purgeExpired());
        }

        @Test
        void isScrollValid_trueWhenContractExistsAndIsNotExpired() {
            // given
            when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.of(validHarvestContract()));

            // when / then
            assertTrue(contractService.isScrollValid(CONTRACT_ID));
        }

        @Test
        void isScrollValid_falseWhenContractIsExpired() {
            // given
            when(contractRepository.findById(CONTRACT_ID))
                    .thenReturn(Optional.of(contract(ContractType.HARVEST, NOW)));

            // when / then
            assertFalse(contractService.isScrollValid(CONTRACT_ID));
        }

        @Test
        void isScrollValid_falseWhenContractNoLongerExists() {
            // given
            when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.empty());

            // when / then
            assertFalse(contractService.isScrollValid(CONTRACT_ID));
        }
    }
}
