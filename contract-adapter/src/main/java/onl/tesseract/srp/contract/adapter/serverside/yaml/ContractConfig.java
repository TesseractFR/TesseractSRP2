package onl.tesseract.srp.contract.adapter.serverside.yaml;

import onl.tesseract.srp.contract.domain.model.contract.ContractQualityWeights;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Configuration des contrats lue dans {@code contracts.yml}. Les tables sont indexées par nom de
 * rareté, de grade ou de destinataire ; les noms de PNJ sont indexés en minuscules pour une
 * recherche sans tenir compte de la casse.
 *
 * @param scrollLifetime        durée de vie d'un parchemin, strictement positive
 * @param quantityByRarity      quantité de base et variance par rareté
 * @param quantityRateByQuality coefficient de quantité par qualité minimale, strictement positif
 * @param rarityWeights         poids de tirage par rareté
 * @param qualityWeights        poids de tirage de la qualité minimale des contrats
 * @param dailyTickets          quota quotidien par grade
 * @param npcNames              nom affiché du PNJ par destinataire, clés en minuscules
 */
public record ContractConfig(Duration scrollLifetime, Map<String, RarityQuantity> quantityByRarity,
                             Map<String, Double> quantityRateByQuality, Map<String, Double> rarityWeights,
                             ContractQualityWeights qualityWeights, Map<String, Integer> dailyTickets,
                             Map<String, String> npcNames) {

    public static final Duration DEFAULT_SCROLL_LIFETIME = Duration.ofHours(72);

    /** Configuration utilisée quand le fichier est absent : toutes les valeurs par défaut. */
    public static final ContractConfig DEFAULT =
            new ContractConfig(DEFAULT_SCROLL_LIFETIME, Map.of(), Map.of(), Map.of(), ContractQualityWeights.DEFAULT,
                    Map.of(), Map.of());

    /** Vérifie la durée de vie et les coefficients de quantité, puis fige les tables. */
    public ContractConfig {
        Objects.requireNonNull(scrollLifetime, "scrollLifetime");
        Objects.requireNonNull(qualityWeights, "qualityWeights");
        if (scrollLifetime.isNegative() || scrollLifetime.isZero()) {
            throw new IllegalArgumentException("La durée de vie d'un parchemin doit être strictement positive : " + scrollLifetime);
        }
        quantityRateByQuality.forEach((qualityName, rate) -> {
            if (!(rate > 0)) {
                throw new IllegalArgumentException("Le coefficient de quantité de " + qualityName
                        + " doit être strictement positif : " + rate);
            }
        });
        quantityByRarity = Map.copyOf(quantityByRarity);
        quantityRateByQuality = Map.copyOf(quantityRateByQuality);
        rarityWeights = Map.copyOf(rarityWeights);
        dailyTickets = Map.copyOf(dailyTickets);
        npcNames = Map.copyOf(npcNames);
    }

    /** @return quantité de base et variance de cette rareté, 300 sans variance si elle n'est pas configurée */
    public RarityQuantity getRarityQuantity(String rarityName) {
        return quantityByRarity.getOrDefault(rarityName, RarityQuantity.DEFAULT);
    }

    /** @return coefficient de quantité de cette qualité, 1 si elle n'est pas configurée */
    public double getQuantityRate(String qualityName) {
        return quantityRateByQuality.getOrDefault(qualityName, 1.0);
    }

    /** @return poids de tirage de cette rareté, zéro si elle n'est pas configurée */
    public double getRarityWeight(String rarityName) {
        return rarityWeights.getOrDefault(rarityName, 0.0);
    }

    /** @return quota quotidien du grade, zéro s'il n'est pas configuré */
    public int getDailyTickets(String rankName) {
        return dailyTickets.getOrDefault(rankName, 0);
    }

    /** @return nom du PNJ du destinataire, sans tenir compte de la casse */
    public Optional<String> findNpcName(String clientName) {
        return Optional.ofNullable(npcNames.get(clientName.toLowerCase(Locale.ROOT)));
    }
}
