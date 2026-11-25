package onl.tesseract.srp.commission.domain.model.commission;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuantityRateTest {

    @Test
    void applyTo_roundsToTheNearestUnitWithAMinimumOfOne() {
        // given
        QuantityRate quarter = new QuantityRate(0.25);

        // when / then
        assertEquals(150, quarter.applyTo(600));
        assertEquals(1, quarter.applyTo(1));
    }

    @Test
    void constructor_throwsWhenTheRateIsNotPositive() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new QuantityRate(0));
    }
}
