package onl.tesseract.srp.commission.domain.model.commission;

import onl.tesseract.srp.commission.domain.model.board.BoardId;
import onl.tesseract.srp.commission.domain.model.board.BoardPart;
import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommissionModelTest {

    private static final CommissionId COMMISSION_ID = new CommissionId(100);
    private static final BoardId BOARD_ID = new BoardId(1);
    private static final PlayerId PLAYER = new PlayerId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final MaterialName WOOD = new MaterialName("BOIS");

    private static Commission commission(int quantity, int deliveredQuantity, int reward) {
        return new Commission(COMMISSION_ID, BOARD_ID, BoardPart.PUBLIC, CommissionType.HARVEST, WOOD, quantity, deliveredQuantity,
                Quality.NORMAL, MoneyValue.of(reward));
    }

    @Test
    void computeRewardShareFor_paysHalfOfTheRewardForHalfOfTheQuantity() {
        // given
        Commission commission = commission(10, 0, 100);

        // when
        int share = commission.computeRewardShareFor(5);

        // then
        assertEquals(50, share);
    }

    @Test
    void computeRewardShareFor_addsUpToTheRewardWhateverTheSplit() {
        // given : 7 items pour 10 lys, livrés un par un, ce qui donne des parts non entières
        int totalPaid = 0;

        // when
        for (int deliveredQuantity = 0; deliveredQuantity < 7; deliveredQuantity++) {
            totalPaid += commission(7, deliveredQuantity, 10).computeRewardShareFor(1);
        }

        // then
        assertEquals(10, totalPaid);
    }

    @Test
    void constructor_throwsWhenDeliveredQuantityExceedsQuantity() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> commission(10, 11, 100));
    }

    @Test
    void isCompleted_isTrueOnlyOnceTheWholeQuantityIsDelivered() {
        // when / then
        assertTrue(commission(10, 10, 100).isCompleted());
        assertFalse(commission(10, 9, 100).isCompleted());
    }

    @Test
    void getItemsValue_addsTheTaxBackToTheEarnings() {
        // given
        Delivery delivery = new Delivery(COMMISSION_ID, BOARD_ID, PLAYER, 10, 75, 25, 0, false, true);

        // when
        int itemsValue = delivery.getItemsValue();

        // then
        assertEquals(100, itemsValue);
    }
}
