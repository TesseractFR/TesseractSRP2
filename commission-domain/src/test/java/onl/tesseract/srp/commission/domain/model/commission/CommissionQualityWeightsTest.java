package onl.tesseract.srp.commission.domain.model.commission;

import onl.tesseract.srp.common.domain.model.enums.Quality;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommissionQualityWeightsTest {

    @Test
    void pick_drawsEachQualityInProportionToItsWeight() {
        // given : NORMAL occupe le premier quart de l'intervalle, GOOD les trois quarts suivants
        CommissionQualityWeights weights = new CommissionQualityWeights(Map.of(Quality.NORMAL, 1.0, Quality.GOOD, 3.0));

        // when / then
        assertEquals(Quality.NORMAL, weights.pick(0.2));
        assertEquals(Quality.GOOD, weights.pick(0.3));
        assertEquals(Quality.GOOD, weights.pick(0.99));
    }

    @Test
    void pick_neverDrawsAQualityWithoutWeight() {
        // given
        CommissionQualityWeights weights = new CommissionQualityWeights(Map.of(Quality.POOR, 0.0, Quality.EXCEPTIONAL, 1.0));

        // when / then
        assertEquals(Quality.EXCEPTIONAL, weights.pick(0.0));
    }

    @Test
    void constructor_throwsWhenNoQualityHasAPositiveWeight() {
        // given
        Map<Quality, Double> weights = Map.of(Quality.POOR, 0.0);

        // when / then
        assertThrows(IllegalArgumentException.class, () -> new CommissionQualityWeights(weights));
    }
}
