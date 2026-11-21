package onl.tesseract.srp.player.domain.port.userside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.player.domain.model.SrpPlayer;
import onl.tesseract.srp.player.domain.model.PlayerRank;
import onl.tesseract.srp.player.domain.port.serverside.PlayerLedgerPort;
import onl.tesseract.srp.player.domain.port.serverside.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlayerMoneyServiceTest {

    private static final PlayerId PLAYER = new PlayerId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

    private PlayerRepository repository;
    private PlayerLedgerPort playerLedgerPort;
    private PlayerMoneyService service;

    @BeforeEach
    void setUp() {
        repository = mock(PlayerRepository.class);
        playerLedgerPort = mock(PlayerLedgerPort.class);
        service = new PlayerMoneyService(new SrpPlayerProfileService(repository), playerLedgerPort);
    }

    private SrpPlayer storedPlayerWith(int amount) {
        SrpPlayer player = new SrpPlayer(PLAYER, PlayerRank.Survivant, amount, "Survivant", 0);
        when(repository.findById(PLAYER)).thenReturn(Optional.of(player));
        return player;
    }

    @Test
    void adjustMoney_addsPositiveAmountAndSaves() {
        // given
        SrpPlayer player = storedPlayerWith(100);

        // when
        boolean adjusted = service.adjustMoney(PLAYER, 50);

        // then
        assertTrue(adjusted);
        assertEquals(150, player.getMoney());
        verify(repository).save(player);
    }

    @Test
    void adjustMoney_withdrawsDownToZero() {
        // given
        SrpPlayer player = storedPlayerWith(100);

        // when
        boolean adjusted = service.adjustMoney(PLAYER, -100);

        // then
        assertTrue(adjusted);
        assertEquals(0, player.getMoney());
        verify(repository).save(player);
    }

    @Test
    void adjustMoney_returnsFalseWithoutSavingWhenResultWouldBeNegative() {
        // given
        SrpPlayer player = storedPlayerWith(100);

        // when
        boolean adjusted = service.adjustMoney(PLAYER, -101);

        // then
        assertFalse(adjusted);
        assertEquals(100, player.getMoney());
        verify(repository, never()).save(any());
    }

    @Test
    void adjustMoney_doesNotRecordInLedger() {
        // given
        storedPlayerWith(100);

        // when
        service.adjustMoney(PLAYER, 50);

        // then
        verifyNoInteractions(playerLedgerPort);
    }

    @Test
    void adjustMoneyAsStaff_adjustsSavesAndRecordsInLedger() {
        // given
        SrpPlayer player = storedPlayerWith(100);

        // when
        boolean adjusted = service.adjustMoneyAsStaff(PLAYER, -40);

        // then
        assertTrue(adjusted);
        assertEquals(60, player.getMoney());
        verify(repository).save(player);
        verify(playerLedgerPort).recordStaffMoneyAdjustment(PLAYER, -40);
    }

    @Test
    void adjustMoneyAsStaff_returnsFalseWithoutRecordingWhenResultWouldBeNegative() {
        // given
        SrpPlayer player = storedPlayerWith(100);

        // when
        boolean adjusted = service.adjustMoneyAsStaff(PLAYER, -101);

        // then
        assertFalse(adjusted);
        assertEquals(100, player.getMoney());
        verify(repository, never()).save(any());
        verifyNoInteractions(playerLedgerPort);
    }
}
