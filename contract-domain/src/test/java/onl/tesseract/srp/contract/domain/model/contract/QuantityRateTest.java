package onl.tesseract.srp.contract.domain.model.contract;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuantityRateTest {

    @Test
    void constructor_rejectsZero() {
        assertThrows(IllegalArgumentException.class, () -> new QuantityRate(0));
    }

    @Test
    void constructor_rejectsNegativeValue() {
        assertThrows(IllegalArgumentException.class, () -> new QuantityRate(-0.5));
    }

    @Test
    void constructor_rejectsNaN() {
        assertThrows(IllegalArgumentException.class, () -> new QuantityRate(Double.NaN));
    }

    @Test
    void applyTo_keepsQuantityWhenNeutral() {
        assertEquals(40, QuantityRate.NEUTRAL.applyTo(40));
    }

    @Test
    void applyTo_reducesQuantity() {
        assertEquals(10, new QuantityRate(0.25).applyTo(40));
    }

    @Test
    void applyTo_roundsToNearestUnit() {
        assertEquals(3, new QuantityRate(0.25).applyTo(10));
    }

    @Test
    void applyTo_neverGoesBelowOne() {
        assertEquals(1, new QuantityRate(0.1).applyTo(2));
    }
}
