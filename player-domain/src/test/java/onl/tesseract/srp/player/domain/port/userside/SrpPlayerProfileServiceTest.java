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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SrpPlayerProfileServiceTest {

    private static final PlayerId PLAYER = new PlayerId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

    private PlayerRepository repository;
    private SrpPlayerProfileService service;

    @BeforeEach
    void setUp() {
        repository = mock(PlayerRepository.class);
        service = new SrpPlayerProfileService(repository);
    }

    @Test
    void getPlayer_returnsStoredPlayer() {
        // given
        SrpPlayer stored = new SrpPlayer(PLAYER, PlayerRank.Noble, 120, "Noble", 3);
        when(repository.findById(PLAYER)).thenReturn(Optional.of(stored));

        // when
        SrpPlayer player = service.getPlayer(PLAYER);

        // then
        assertSame(stored, player);
    }

    @Test
    void getPlayer_returnsDefaultPlayerWithoutSavingWhenNothingIsStored() {
        // given
        when(repository.findById(PLAYER)).thenReturn(Optional.empty());

        // when
        SrpPlayer player = service.getPlayer(PLAYER);

        // then
        assertEquals(PLAYER, player.getUniqueId());
        assertEquals(PlayerRank.Survivant, player.getRank());
        assertEquals(PlayerRank.Survivant.getTitle().getId(), player.getTitleID());
        assertEquals(0, player.getMoney());
        assertEquals(0, player.getIlluminationPoints());
        verify(repository, never()).save(any());
    }

    @Test
    void savePlayer_delegatesToRepository() {
        // given
        SrpPlayer player = new SrpPlayer(PLAYER);

        // when
        service.savePlayer(player);

        // then
        verify(repository).save(player);
    }
}
