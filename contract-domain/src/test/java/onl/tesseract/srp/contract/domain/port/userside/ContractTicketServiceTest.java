package onl.tesseract.srp.contract.domain.port.userside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.model.exception.InvalidTicketAmountException;
import onl.tesseract.srp.contract.domain.port.serverside.ContractTicketRepository;
import onl.tesseract.srp.contract.domain.port.serverside.DailyTicketQuotaPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.OptionalInt;
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

class ContractTicketServiceTest {

    private static final PlayerId PLAYER = new PlayerId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

    private ContractTicketRepository contractTicketRepository;
    private DailyTicketQuotaPort dailyTicketQuotaPort;
    private ContractTicketService contractTicketService;

    @BeforeEach
    void setUp() {
        contractTicketRepository = mock(ContractTicketRepository.class);
        dailyTicketQuotaPort = mock(DailyTicketQuotaPort.class);
        contractTicketService = new ContractTicketService(contractTicketRepository, dailyTicketQuotaPort);
    }

    @Test
    void getTickets_returnsSavedBalance() {
        // given
        when(contractTicketRepository.findTickets(PLAYER)).thenReturn(OptionalInt.of(7));

        // when / then
        assertEquals(7, contractTicketService.getTickets(PLAYER));
    }

    @Test
    void getTickets_returnsDailyQuotaForNewPlayer() {
        // given
        when(contractTicketRepository.findTickets(PLAYER)).thenReturn(OptionalInt.empty());
        when(dailyTicketQuotaPort.getDailyQuota(PLAYER)).thenReturn(4);

        // when / then
        assertEquals(4, contractTicketService.getTickets(PLAYER));
    }

    @Test
    void hasTicket_falseWhenBalanceIsZero() {
        // given
        when(contractTicketRepository.findTickets(PLAYER)).thenReturn(OptionalInt.of(0));

        // when / then
        assertFalse(contractTicketService.hasTicket(PLAYER));
    }

    @Test
    void hasTicket_trueWhenNewPlayerHasDailyQuota() {
        // given
        when(contractTicketRepository.findTickets(PLAYER)).thenReturn(OptionalInt.empty());
        when(dailyTicketQuotaPort.getDailyQuota(PLAYER)).thenReturn(2);

        // when / then
        assertTrue(contractTicketService.hasTicket(PLAYER));
    }

    @Test
    void withdrawTicket_decrementsBalance() {
        // given
        when(contractTicketRepository.findTickets(PLAYER)).thenReturn(OptionalInt.of(3));

        // when
        contractTicketService.withdrawTicket(PLAYER);

        // then
        verify(contractTicketRepository).saveTickets(PLAYER, 2);
    }

    @Test
    void refundTicket_incrementsBalance() {
        // given
        when(contractTicketRepository.findTickets(PLAYER)).thenReturn(OptionalInt.of(2));

        // when
        contractTicketService.refundTicket(PLAYER);

        // then
        verify(contractTicketRepository).saveTickets(PLAYER, 3);
    }

    @Test
    void giveTickets_addsAmountToBalanceWithoutCap() {
        // given
        when(contractTicketRepository.findTickets(PLAYER)).thenReturn(OptionalInt.of(50));

        // when
        contractTicketService.giveTickets(PLAYER, 100);

        // then
        verify(contractTicketRepository).saveTickets(PLAYER, 150);
    }

    @Test
    void giveTickets_throwsWhenAmountIsNotStrictlyPositive() {
        // when
        InvalidTicketAmountException thrown = assertThrows(InvalidTicketAmountException.class,
                () -> contractTicketService.giveTickets(PLAYER, 0));

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
        contractTicketService.renewDaily(PLAYER);

        // then
        verify(contractTicketRepository).saveTickets(PLAYER, 3);
    }

    @Test
    void renewDaily_doesNothingWhenBalanceAlreadyEqualsQuota() {
        // given
        when(dailyTicketQuotaPort.getDailyQuota(PLAYER)).thenReturn(3);
        when(contractTicketRepository.findTickets(PLAYER)).thenReturn(OptionalInt.of(3));

        // when
        contractTicketService.renewDaily(PLAYER);

        // then
        verify(contractTicketRepository, never()).saveTickets(any(), anyInt());
    }
}
