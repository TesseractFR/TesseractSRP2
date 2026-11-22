package onl.tesseract.srp.player.domain.port.userside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.player.domain.model.SrpPlayer;
import onl.tesseract.srp.player.domain.model.PlayerRank;
import onl.tesseract.srp.player.domain.port.serverside.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PlayerIlluminationServiceTest {

    private static final PlayerId PLAYER = new PlayerId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

    private PlayerRepository repository;
    private PlayerIlluminationService service;

    @BeforeEach
    void setUp() {
        repository = mock(PlayerRepository.class);
        service = new PlayerIlluminationService(new SrpPlayerProfileService(repository));
    }

    private SrpPlayer storedPlayerWith(int amount) {
        SrpPlayer player = new SrpPlayer(PLAYER, PlayerRank.Survivant, 0, "Survivant", amount);
        when(repository.findById(PLAYER)).thenReturn(Optional.of(player));
        return player;
    }

    @Test
    void addIlluminationPoints_addsAmountAndSaves() {
        // given
        SrpPlayer player = storedPlayerWith(100);

        // when
        service.addIlluminationPoints(PLAYER, 50);

        // then
        assertEquals(150, player.getIlluminationPoints());
        verify(repository).save(player);
    }

    @Test
    void addIlluminationPoints_throwsWhenAmountIsNegative() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> service.addIlluminationPoints(PLAYER, -1));
        verifyNoInteractions(repository);
    }

    @Test
    void removeIlluminationPoints_withdrawsDownToZero() {
        // given
        SrpPlayer player = storedPlayerWith(100);

        // when
        boolean removed = service.removeIlluminationPoints(PLAYER, 100);

        // then
        assertTrue(removed);
        assertEquals(0, player.getIlluminationPoints());
        verify(repository).save(player);
    }

    @Test
    void removeIlluminationPoints_returnsFalseWithoutSavingWhenPointsAreNotEnough() {
        // given
        SrpPlayer player = storedPlayerWith(100);

        // when
        boolean removed = service.removeIlluminationPoints(PLAYER, 101);

        // then
        assertFalse(removed);
        assertEquals(100, player.getIlluminationPoints());
        verify(repository, never()).save(any());
    }

    @Test
    void removeIlluminationPoints_throwsWhenAmountIsNegative() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> service.removeIlluminationPoints(PLAYER, -1));
        verifyNoInteractions(repository);
    }
}
