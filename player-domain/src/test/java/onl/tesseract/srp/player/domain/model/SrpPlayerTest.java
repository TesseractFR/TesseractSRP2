package onl.tesseract.srp.player.domain.model;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.exception.NotEnoughMoneyException;
import onl.tesseract.srp.player.domain.model.exception.NotEnoughIlluminationPointsException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SrpPlayerTest {

    private static final PlayerId PLAYER = new PlayerId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

    private SrpPlayer playerWith(PlayerRank rank, int money) {
        return new SrpPlayer(PLAYER, rank, money, rank.getTitle().getId(), 0);
    }

    @Test
    void constructor_givesDefaultValuesToNewPlayer() {
        // when
        SrpPlayer player = new SrpPlayer(PLAYER);

        // then
        assertEquals(PlayerRank.Survivant, player.getRank());
        assertEquals(PlayerRank.Survivant.getTitle().getId(), player.getTitleID());
        assertEquals(0, player.getMoney());
        assertEquals(0, player.getIlluminationPoints());
    }

    @Test
    void addMoney_addsPositiveAmount() {
        // given
        SrpPlayer player = new SrpPlayer(PLAYER);

        // when
        int money = player.addMoney(10);

        // then
        assertEquals(10, money);
        assertEquals(10, player.getMoney());
    }

    @Test
    void addMoney_withdrawsWhenBalanceIsEnough() {
        // given
        SrpPlayer player = playerWith(PlayerRank.Survivant, 20);

        // when
        player.addMoney(-10);

        // then
        assertEquals(10, player.getMoney());
    }

    @Test
    void addMoney_throwsWithoutChangingBalanceWhenBalanceIsNotEnough() {
        // given
        SrpPlayer player = playerWith(PlayerRank.Survivant, 20);

        // when / then
        assertThrows(NotEnoughMoneyException.class, () -> player.addMoney(-50));
        assertEquals(20, player.getMoney());
    }

    @Test
    void addIlluminationPoints_throwsWithoutChangingPointsWhenPointsAreNotEnough() {
        // given
        SrpPlayer player = new SrpPlayer(PLAYER, PlayerRank.Survivant, 0, "Survivant", 5);

        // when / then
        assertThrows(NotEnoughIlluminationPointsException.class, () -> player.addIlluminationPoints(-6));
        assertEquals(5, player.getIlluminationPoints());
    }

    @Test
    void buyNextRank_updatesRankAndChargesCostWhenPlayerHasEnoughMoney() {
        // given
        SrpPlayer player = playerWith(PlayerRank.Survivant, 800);

        // when
        boolean bought = player.buyNextRank();

        // then
        assertTrue(bought);
        assertEquals(PlayerRank.Explorateur, player.getRank());
        assertEquals(800 - PlayerRank.Explorateur.getCost(), player.getMoney());
    }

    @Test
    void buyNextRank_changesNothingWhenPlayerHasLastRank() {
        // given
        SrpPlayer player = playerWith(PlayerRank.Empereur, 1_000_000_000);

        // when
        boolean bought = player.buyNextRank();

        // then
        assertFalse(bought);
        assertEquals(PlayerRank.Empereur, player.getRank());
        assertEquals(1_000_000_000, player.getMoney());
    }

    @Test
    void buyNextRank_changesNothingWhenPlayerHasNotEnoughMoney() {
        // given
        SrpPlayer player = playerWith(PlayerRank.Survivant, 10);

        // when
        boolean bought = player.buyNextRank();

        // then
        assertFalse(bought);
        assertEquals(PlayerRank.Survivant, player.getRank());
        assertEquals(10, player.getMoney());
    }
}
