package onl.tesseract.srp.commission.domain.model.commission;

import onl.tesseract.srp.common.domain.model.enums.Quality;

import java.util.EnumMap;
import java.util.Map;

/**
 * Poids de tirage de la qualité minimale demandée par une commission générée : une qualité sort
 * proportionnellement à son poids. Une qualité absente de la table a un poids nul et n'est jamais tirée.
 *
 * @param weights poids par qualité, positifs ou nuls, dont au moins un strictement positif
 */
public record CommissionQualityWeights(Map<Quality, Double> weights) {

    public CommissionQualityWeights {
        weights.forEach((quality, weight) -> {
            if (weight < 0) {
                throw new IllegalArgumentException("Le poids de la qualité " + quality
                        + " doit être un nombre positif ou nul : " + weight);
            }
        });
        if (weights.values().stream().mapToDouble(Double::doubleValue).sum() <= 0) {
            throw new IllegalArgumentException("Au moins une qualité doit avoir un poids strictement positif : " + weights);
        }
        weights = Map.copyOf(new EnumMap<>(weights));
    }

    /** @return poids de la qualité, zéro si elle n'est pas configurée */
    public double weightOf(Quality quality) {
        return weights.getOrDefault(quality, 0.0);
    }

    /**
     * Choisit la qualité correspondant à un tirage uniforme : de la plus basse à la plus haute, chaque qualité
     * occupe une part de l'intervalle proportionnelle à son poids.
     *
     * @param uniformDraw tirage uniforme dans [0 ; 1[
     * @return la qualité tirée, jamais une qualité de poids nul
     */
    public Quality pick(double uniformDraw) {
        double remainingWeight = uniformDraw * weights.values().stream().mapToDouble(Double::doubleValue).sum();
        // Toujours remplacée dans la boucle : le constructeur garantit au moins un poids strictement positif.
        Quality lastDrawableQuality = Quality.values()[0];
        for (Quality quality : Quality.values()) {
            double weight = weightOf(quality);
            if (weight > 0) {
                lastDrawableQuality = quality;
                remainingWeight -= weight;
                if (remainingWeight < 0) {
                    return quality;
                }
            }
        }
        // Arrondi flottant sur un tirage proche de 1 : la plus haute qualité tirable.
        return lastDrawableQuality;
    }
}
