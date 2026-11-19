package onl.tesseract.srp.contract.domain.model.contract;

import onl.tesseract.srp.common.domain.model.enums.Quality;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContractQualityWeightsTest {

    @Test
    void default_makesHighQualitiesRarer() {
        // given
        ContractQualityWeights weights = ContractQualityWeights.DEFAULT;

        // when / then : 30, 35, 20, 10, 5
        assertEquals(30, weights.weightOf(Quality.POOR));
        assertEquals(35, weights.weightOf(Quality.NORMAL));
        assertEquals(20, weights.weightOf(Quality.GOOD));
        assertEquals(10, weights.weightOf(Quality.VERY_GOOD));
        assertEquals(5, weights.weightOf(Quality.EXCEPTIONAL));
    }

    @Test
    void weightOf_returnsZeroForMissingQuality() {
        // given
        ContractQualityWeights weights = new ContractQualityWeights(Map.of(Quality.POOR, 1.0));

        // when / then
        assertEquals(0, weights.weightOf(Quality.EXCEPTIONAL));
    }

    @ParameterizedTest
    @CsvSource({
            // parts sur 100 : POOR [0 ; 30[, NORMAL [30 ; 65[, GOOD [65 ; 85[, VERY_GOOD [85 ; 95[, EXCEPTIONAL [95 ; 100[
            "0.0, POOR",
            "0.29, POOR",
            "0.31, NORMAL",
            "0.64, NORMAL",
            "0.66, GOOD",
            "0.84, GOOD",
            "0.86, VERY_GOOD",
            "0.94, VERY_GOOD",
            "0.96, EXCEPTIONAL",
            "0.999, EXCEPTIONAL"
    })
    void pick_returnsQualityMatchingDrawShare(double uniformDraw, Quality expectedQuality) {
        // when / then
        assertEquals(expectedQuality, ContractQualityWeights.DEFAULT.pick(uniformDraw));
    }

    @Test
    void pick_drawsEachQualityInProportionToItsWeight() {
        // given : tirages répartis uniformément sur [0 ; 1[
        int drawCount = 10_000;
        Map<Quality, Integer> occurrences = new EnumMap<>(Quality.class);

        // when
        for (int index = 0; index < drawCount; index++) {
            Quality quality = ContractQualityWeights.DEFAULT.pick((index + 0.5) / drawCount);
            occurrences.merge(quality, 1, Integer::sum);
        }

        // then
        assertEquals(3_000, occurrences.get(Quality.POOR));
        assertEquals(3_500, occurrences.get(Quality.NORMAL));
        assertEquals(2_000, occurrences.get(Quality.GOOD));
        assertEquals(1_000, occurrences.get(Quality.VERY_GOOD));
        assertEquals(500, occurrences.get(Quality.EXCEPTIONAL));
    }

    @Test
    void pick_neverReturnsQualityWithZeroWeight() {
        // given
        ContractQualityWeights weights = new ContractQualityWeights(
                Map.of(Quality.NORMAL, 1.0, Quality.GOOD, 1.0, Quality.EXCEPTIONAL, 0.0));

        // when / then
        for (int index = 0; index < 1_000; index++) {
            Quality quality = weights.pick(index / 1_000.0);
            assertTrue(quality == Quality.NORMAL || quality == Quality.GOOD, "qualité inattendue : " + quality);
        }
    }

    @ParameterizedTest
    @ValueSource(doubles = {-0.1, 1.0, 1.5})
    void pick_throwsWhenDrawIsOutsideUnitInterval(double uniformDraw) {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> ContractQualityWeights.DEFAULT.pick(uniformDraw));
    }

    @Test
    void constructor_throwsWhenWeightIsNegative() {
        // given
        Map<Quality, Double> weights = Map.of(Quality.POOR, 10.0, Quality.GOOD, -1.0);

        // when / then
        assertThrows(IllegalArgumentException.class, () -> new ContractQualityWeights(weights));
    }

    @Test
    void constructor_throwsWhenWeightIsNull() {
        // given
        Map<Quality, Double> weights = new HashMap<>();
        weights.put(Quality.POOR, 10.0);
        weights.put(Quality.GOOD, null);

        // when / then
        assertThrows(IllegalArgumentException.class, () -> new ContractQualityWeights(weights));
    }

    @Test
    void constructor_throwsWhenAllWeightsAreZero() {
        // given
        Map<Quality, Double> weights = Map.of(Quality.POOR, 0.0, Quality.GOOD, 0.0);

        // when / then
        assertThrows(IllegalArgumentException.class, () -> new ContractQualityWeights(weights));
    }

    @Test
    void constructor_throwsWhenNoWeightIsGiven() {
        // given
        Map<Quality, Double> weights = Map.of();

        // when / then
        assertThrows(IllegalArgumentException.class, () -> new ContractQualityWeights(weights));
    }
}
