package onl.tesseract.srp.contract.domain.port.userside;

import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.contract.ContractClient;
import onl.tesseract.srp.contract.domain.model.contract.ContractId;
import onl.tesseract.srp.contract.domain.model.contract.ContractType;
import onl.tesseract.srp.contract.domain.model.exception.ContractExpiredException;
import onl.tesseract.srp.contract.domain.model.exception.ContractNotFoundException;
import onl.tesseract.srp.contract.domain.model.exception.ContractScrollNotOwnedException;
import onl.tesseract.srp.contract.domain.model.exception.MissingContractItemsException;
import onl.tesseract.srp.contract.domain.model.exception.PaymentFailedException;
import onl.tesseract.srp.contract.domain.model.exception.WrongContractClientException;
import onl.tesseract.srp.contract.domain.port.serverside.ContractRepository;
import onl.tesseract.srp.contract.domain.port.serverside.ContractScrollPort;
import onl.tesseract.srp.contract.domain.port.serverside.JobMoneyBonusPort;
import onl.tesseract.srp.contract.domain.port.serverside.PaymentPort;
import onl.tesseract.srp.contract.domain.port.serverside.PlayerMaterialPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContractDeliveryServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-15T12:00:00Z");
    private static final PlayerId PLAYER = new PlayerId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final ContractId CONTRACT_ID = new ContractId(42);
    private static final ContractClient FARMER = new ContractClient("farmer");
    private static final MaterialName WHEAT = new MaterialName("wheat");

    private static Contract contract(ContractType type, Instant expiresAt) {
        return new Contract(CONTRACT_ID, type, FARMER, WHEAT, 10, Quality.POOR, MoneyValue.of(100), expiresAt);
    }

    private static Contract validHarvestContract() {
        return contract(ContractType.HARVEST, NOW.plusSeconds(3600));
    }

    private ContractRepository contractRepository;
    private ContractScrollPort contractScrollPort;
    private PlayerMaterialPort playerMaterialPort;
    private JobMoneyBonusPort jobMoneyBonusPort;
    private PaymentPort paymentPort;
    private ContractDeliveryService contractDeliveryService;

    @BeforeEach
    void setUp() {
        contractRepository = mock(ContractRepository.class);
        contractScrollPort = mock(ContractScrollPort.class);
        playerMaterialPort = mock(PlayerMaterialPort.class);
        jobMoneyBonusPort = mock(JobMoneyBonusPort.class);
        paymentPort = mock(PaymentPort.class);
        contractDeliveryService = new ContractDeliveryService(contractRepository, contractScrollPort, playerMaterialPort,
                jobMoneyBonusPort, paymentPort, Clock.fixed(NOW, ZoneOffset.UTC));

        when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.of(validHarvestContract()));
        when(contractScrollPort.hasScroll(PLAYER, CONTRACT_ID)).thenReturn(true);
        when(contractScrollPort.removeScroll(PLAYER, CONTRACT_ID)).thenReturn(true);
        when(playerMaterialPort.countAtLeast(PLAYER, WHEAT, Quality.POOR)).thenReturn(10);
    }

    private static void assertMoney(int expected, MoneyValue actual) {
        assertEquals(0, BigDecimal.valueOf(expected).compareTo(actual.value()),
                () -> "attendu " + expected + ", obtenu " + actual.value());
    }

    @Test
    void deliver_removesScrollAndItemsDeletesContractAndPaysReward() {
        // when
        MoneyValue payment = contractDeliveryService.deliver(PLAYER, CONTRACT_ID, FARMER);

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
        MoneyValue payment = contractDeliveryService.deliver(PLAYER, CONTRACT_ID, FARMER);

        // then : 100 + 5 %
        assertMoney(105, payment);
    }

    @Test
    void getDeliverableContract_returnsContractWhenNpcIsTheContractClient() {
        // when
        Contract contract = contractDeliveryService.getDeliverableContract(CONTRACT_ID, FARMER);

        // then
        assertEquals(CONTRACT_ID, contract.id());
    }

    @Test
    void getDeliverableContract_throwsWhenNpcIsNotTheContractClient() {
        // when
        WrongContractClientException thrown = assertThrows(WrongContractClientException.class,
                () -> contractDeliveryService.getDeliverableContract(CONTRACT_ID, new ContractClient("miner")));

        // then
        assertEquals(FARMER, thrown.getExpectedClient());
    }

    @Test
    void getDeliverableContract_throwsWhenContractDoesNotExist() {
        // given
        when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.empty());

        // when / then
        assertThrows(ContractNotFoundException.class,
                () -> contractDeliveryService.getDeliverableContract(CONTRACT_ID, FARMER));
    }

    @Test
    void deliver_throwsWhenContractDoesNotExist() {
        // given
        when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.empty());

        // when / then
        assertThrows(ContractNotFoundException.class, () -> contractDeliveryService.deliver(PLAYER, CONTRACT_ID, FARMER));
        verify(paymentPort, never()).pay(any(), any());
    }

    @Test
    void deliver_throwsAndDeletesContractWhenExpired() {
        // given
        when(contractRepository.findById(CONTRACT_ID))
                .thenReturn(Optional.of(contract(ContractType.HARVEST, NOW.minusSeconds(1))));

        // when / then
        assertThrows(ContractExpiredException.class, () -> contractDeliveryService.deliver(PLAYER, CONTRACT_ID, FARMER));
        verify(contractRepository).delete(CONTRACT_ID);
        verify(contractScrollPort, never()).removeScroll(any(), any());
    }

    @Test
    void deliver_throwsWhenNpcIsNotTheContractClient() {
        // when
        WrongContractClientException thrown = assertThrows(WrongContractClientException.class,
                () -> contractDeliveryService.deliver(PLAYER, CONTRACT_ID, new ContractClient("miner")));

        // then
        assertEquals(FARMER, thrown.getExpectedClient());
        verify(contractScrollPort, never()).removeScroll(any(), any());
    }

    @Test
    void deliver_throwsWhenDelivererDoesNotOwnTheScroll() {
        // given
        when(contractScrollPort.hasScroll(PLAYER, CONTRACT_ID)).thenReturn(false);

        // when / then
        assertThrows(ContractScrollNotOwnedException.class,
                () -> contractDeliveryService.deliver(PLAYER, CONTRACT_ID, FARMER));
        verify(playerMaterialPort, never()).removeAtLeast(any(), any(), any(), anyInt());
    }

    @Test
    void deliver_throwsWithMissingQuantityWhenItemsAreMissing() {
        // given
        when(playerMaterialPort.countAtLeast(PLAYER, WHEAT, Quality.POOR)).thenReturn(7);

        // when
        MissingContractItemsException thrown = assertThrows(MissingContractItemsException.class,
                () -> contractDeliveryService.deliver(PLAYER, CONTRACT_ID, FARMER));

        // then
        assertEquals(3, thrown.getMissingQuantity());
        verify(contractScrollPort, never()).removeScroll(any(), any());
    }

    @Test
    void deliver_throwsWithoutRemovingItemsWhenScrollRemovalFails() {
        // given
        when(contractScrollPort.removeScroll(PLAYER, CONTRACT_ID)).thenReturn(false);

        // when / then
        assertThrows(ContractScrollNotOwnedException.class,
                () -> contractDeliveryService.deliver(PLAYER, CONTRACT_ID, FARMER));
        verify(playerMaterialPort, never()).removeAtLeast(any(), any(), any(), anyInt());
    }

    @Test
    void deliver_propagatesPaymentFailureAfterRemovingItems() {
        // given
        doThrow(new PaymentFailedException(PLAYER, MoneyValue.of(100))).when(paymentPort).pay(any(), any());

        // when / then
        assertThrows(PaymentFailedException.class, () -> contractDeliveryService.deliver(PLAYER, CONTRACT_ID, FARMER));
        verify(playerMaterialPort).removeAtLeast(PLAYER, WHEAT, Quality.POOR, 10);
        verify(contractRepository).delete(CONTRACT_ID);
    }

    @Test
    void findMoneyBonus_usesDelivererBonusRate() {
        // given
        when(jobMoneyBonusPort.getMoneyBonusRate(PLAYER, FARMER, WHEAT)).thenReturn(0.05);

        // when
        Optional<MoneyValue> bonus = contractDeliveryService.findMoneyBonus(PLAYER, validHarvestContract());

        // then
        assertMoney(5, bonus.orElseThrow());
    }

    @Test
    void hasEnoughItems_trueWhenOwnedQuantityReachesContractQuantity() {
        assertTrue(contractDeliveryService.hasEnoughItems(PLAYER, validHarvestContract()));
    }

    @Test
    void hasEnoughItems_falseWhenOwnedQuantityIsBelowContractQuantity() {
        // given
        when(playerMaterialPort.countAtLeast(PLAYER, WHEAT, Quality.POOR)).thenReturn(9);

        // when / then
        assertFalse(contractDeliveryService.hasEnoughItems(PLAYER, validHarvestContract()));
    }
}
