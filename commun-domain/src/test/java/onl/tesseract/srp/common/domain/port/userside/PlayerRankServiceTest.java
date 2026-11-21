package onl.tesseract.srp.common.domain.port.userside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.SrpPlayer;
import onl.tesseract.srp.common.domain.model.enums.PlayerRank;
import onl.tesseract.srp.common.domain.port.serverside.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlayerRankServiceTest {

    private static final PlayerId PLAYER = new PlayerId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

    private PlayerRepository repository;
    private PlayerRankService service;

    @BeforeEach
    void setUp() {
        repository = mock(PlayerRepository.class);
        service = new PlayerRankService(new SrpPlayerProfileService(repository));
    }

    private SrpPlayer storedPlayer(PlayerRank rank, int money) {
        SrpPlayer player = new SrpPlayer(PLAYER, rank, money, rank.getTitle().getId(), 0);
        when(repository.getById(PLAYER)).thenReturn(player);
        return player;
    }

    @Test
    void changeRank_updatesRankAndTitleAndSaves() {
        // given
        SrpPlayer player = storedPlayer(PlayerRank.Survivant, 0);

        // when
        boolean changed = service.changeRank(PLAYER, PlayerRank.Baron);

        // then
        assertTrue(changed);
        assertEquals(PlayerRank.Baron, player.getRank());
        assertEquals(PlayerRank.Baron.getTitle().getId(), player.getTitleID());
        verify(repository).save(player);
    }

    @Test
    void changeRank_returnsFalseWithoutSavingWhenRankIsAlreadySet() {
        // given
        storedPlayer(PlayerRank.Baron, 0);

        // when
        boolean changed = service.changeRank(PLAYER, PlayerRank.Baron);

        // then
        assertFalse(changed);
        verify(repository, never()).save(any());
    }

    @Test
    void buyNextRank_chargesCostUpdatesTitleAndSaves() {
        // given
        int cost = PlayerRank.Explorateur.getCost();
        SrpPlayer player = storedPlayer(PlayerRank.Survivant, cost + 10);

        // when
        boolean bought = service.buyNextRank(PLAYER);

        // then
        assertTrue(bought);
        assertEquals(PlayerRank.Explorateur, player.getRank());
        assertEquals(PlayerRank.Explorateur.getTitle().getId(), player.getTitleID());
        assertEquals(10, player.getMoney());
        verify(repository).save(player);
    }

    @Test
    void buyNextRank_returnsFalseWithoutSavingWhenPlayerHasNotEnoughMoney() {
        // given
        SrpPlayer player = storedPlayer(PlayerRank.Survivant, PlayerRank.Explorateur.getCost() - 1);

        // when
        boolean bought = service.buyNextRank(PLAYER);

        // then
        assertFalse(bought);
        assertEquals(PlayerRank.Survivant, player.getRank());
        verify(repository, never()).save(any());
    }

    @Test
    void buyNextRank_returnsFalseWithoutSavingWhenPlayerHasLastRank() {
        // given
        storedPlayer(PlayerRank.Empereur, Integer.MAX_VALUE);

        // when
        boolean bought = service.buyNextRank(PLAYER);

        // then
        assertFalse(bought);
        verify(repository, never()).save(any());
    }
}
