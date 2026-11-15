package onl.tesseract.srp.contract.domain.port.userside;

import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.contract.ContractClient;
import onl.tesseract.srp.contract.domain.model.contract.ContractId;
import onl.tesseract.srp.contract.domain.model.contract.ContractType;
import onl.tesseract.srp.contract.domain.model.contract.ContractDraft;
import onl.tesseract.srp.contract.domain.model.exception.NoContractAvailableException;
import onl.tesseract.srp.contract.domain.model.exception.NoContractTicketLeftException;
import onl.tesseract.srp.contract.domain.model.exception.ScrollInventoryFullException;
import onl.tesseract.srp.contract.domain.port.serverside.ContractRepository;
import onl.tesseract.srp.contract.domain.port.serverside.ContractScrollPort;
import onl.tesseract.srp.contract.domain.port.serverside.JobMoneyBonusPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContractPurchaseServiceTest {

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
    private ContractGenerator contractGenerator;
    private ContractScrollPort contractScrollPort;
    private ContractTicketService contractTicketService;
    private JobMoneyBonusPort jobMoneyBonusPort;
    private ContractPurchaseService contractPurchaseService;

    @BeforeEach
    void setUp() {
        contractRepository = mock(ContractRepository.class);
        contractGenerator = mock(ContractGenerator.class);
        contractScrollPort = mock(ContractScrollPort.class);
        contractTicketService = mock(ContractTicketService.class);
        jobMoneyBonusPort = mock(JobMoneyBonusPort.class);
        contractPurchaseService = new ContractPurchaseService(contractRepository, contractGenerator, contractScrollPort,
                contractTicketService, jobMoneyBonusPort);

        when(contractTicketService.hasTicket(PLAYER)).thenReturn(true);
        when(contractScrollPort.canReceiveScroll(PLAYER)).thenReturn(true);
        when(contractGenerator.generate()).thenReturn(Optional.of(new ContractDraft(ContractType.HARVEST, FARMER, WHEAT,
                10, Quality.POOR, MoneyValue.of(100), NOW.plusSeconds(3600))));
        when(contractRepository.create(any())).thenReturn(validHarvestContract());
        when(contractScrollPort.giveScroll(any(), any(), anyBoolean())).thenReturn(true);
    }

    @Test
    void buyScroll_returnsCreatedContractAndSpendsOneTicket() {
        // when
        Contract contract = contractPurchaseService.buyScroll(PLAYER);

        // then
        assertEquals(CONTRACT_ID, contract.id());
        verify(contractTicketService).withdrawTicket(PLAYER);
        verify(contractTicketService, never()).refundTicket(any());
        verify(contractScrollPort).giveScroll(PLAYER, contract, false);
    }

    @Test
    void buyScroll_throwsWhenPlayerHasNoTicketLeft() {
        // given
        when(contractTicketService.hasTicket(PLAYER)).thenReturn(false);

        // when / then
        assertThrows(NoContractTicketLeftException.class, () -> contractPurchaseService.buyScroll(PLAYER));
        verify(contractTicketService, never()).withdrawTicket(any());
    }

    @Test
    void buyScroll_throwsWhenInventoryCannotReceiveScroll() {
        // given
        when(contractScrollPort.canReceiveScroll(PLAYER)).thenReturn(false);

        // when / then
        assertThrows(ScrollInventoryFullException.class, () -> contractPurchaseService.buyScroll(PLAYER));
        verify(contractTicketService, never()).withdrawTicket(any());
    }

    @Test
    void buyScroll_throwsWhenCatalogIsEmpty() {
        // given
        when(contractGenerator.generate()).thenReturn(Optional.empty());

        // when / then
        assertThrows(NoContractAvailableException.class, () -> contractPurchaseService.buyScroll(PLAYER));
        verify(contractTicketService, never()).withdrawTicket(any());
    }

    @Test
    void buyScroll_refundsTicketWhenSavingFails() {
        // given
        IllegalStateException databaseFailure = new IllegalStateException("base indisponible");
        when(contractRepository.create(any())).thenThrow(databaseFailure);

        // when
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> contractPurchaseService.buyScroll(PLAYER));

        // then
        assertSame(databaseFailure, thrown);
        verify(contractTicketService).refundTicket(PLAYER);
    }

    @Test
    void buyScroll_cancelsPurchaseWhenScrollCannotBeGiven() {
        // given
        when(contractScrollPort.giveScroll(any(), any(), anyBoolean())).thenReturn(false);

        // when / then
        assertThrows(ScrollInventoryFullException.class, () -> contractPurchaseService.buyScroll(PLAYER));
        verify(contractRepository).delete(CONTRACT_ID);
        verify(contractTicketService).refundTicket(PLAYER);
    }

    @Test
    void buyScroll_marksScrollWhenBuyerHasJobBonus() {
        // given
        when(jobMoneyBonusPort.getMoneyBonusRate(PLAYER, FARMER, WHEAT)).thenReturn(0.05);

        // when
        Contract contract = contractPurchaseService.buyScroll(PLAYER);

        // then
        verify(contractScrollPort).giveScroll(PLAYER, contract, true);
    }
}
