package onl.tesseract.srp.commission.adapter.serverside.yaml;

import onl.tesseract.srp.commission.domain.model.board.CommissionBoardUpgradeStep;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardUpgradeType;
import onl.tesseract.srp.commission.domain.model.board.UsageTaxRate;
import onl.tesseract.srp.commission.domain.model.commission.CommissionQualityWeights;
import onl.tesseract.srp.commission.domain.model.commission.QuantityRate;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Configuration des commissions lue dans {@code commissions.yml}. Les raretés sont indexées par nom,
 * les paliers d'amélioration et les taux de bonus d'argent par niveau.
 *
 * @param spawnRewardRate       part de la valeur des items payée au spawn, strictement positive
 * @param guildXpPerLys         XP de guilde accordée par lys de récompense, positive ou nulle
 * @param defaultUsageTaxRate   taxe d'usage d'un tableau de guilde neuf
 * @param spawnSlotsPerType     nombre de commissions par type au spawn, au moins un
 * @param rarityWeights         poids de tirage par rareté
 * @param quantityByRarity      quantité de base et variance par rareté
 * @param qualityWeights        poids de tirage de la qualité minimale des commissions
 * @param quantityRateByQuality coefficient de quantité par qualité minimale
 * @param harvestTiers          tier de chaque item de récolte que les tableaux peuvent demander
 * @param upgradeSteps          paliers de chaque amélioration, par niveau à atteindre
 * @param moneyBonusRates       taux du bonus d'argent par niveau, positifs ou nuls
 */
public record CommissionConfig(double spawnRewardRate, double guildXpPerLys, UsageTaxRate defaultUsageTaxRate,
                               int spawnSlotsPerType, Map<String, Double> rarityWeights,
                               Map<String, CommissionRarityQuantity> quantityByRarity,
                               CommissionQualityWeights qualityWeights, Map<Quality, QuantityRate> quantityRateByQuality,
                               Map<MaterialName, Integer> harvestTiers,
                               Map<CommissionBoardUpgradeType, Map<Integer, CommissionBoardUpgradeStep>> upgradeSteps,
                               Map<Integer, Double> moneyBonusRates) {

    public CommissionConfig {
        if (spawnRewardRate <= 0) {
            throw new IllegalArgumentException("La part payée au spawn doit être strictement positive : " + spawnRewardRate);
        }
        if (guildXpPerLys < 0) {
            throw new IllegalArgumentException("L'XP de guilde par lys ne peut pas être négative : " + guildXpPerLys);
        }
        if (spawnSlotsPerType < 1) {
            throw new IllegalArgumentException("Le spawn a au moins une commission par type : " + spawnSlotsPerType);
        }
        harvestTiers.forEach((material, tier) -> {
            if (tier < 1) {
                throw new IllegalArgumentException("Le tier de " + material.value() + " vaut au moins 1 : " + tier);
            }
        });
        rarityWeights.forEach((rarityName, weight) -> {
            if (weight < 0) {
                throw new IllegalArgumentException("Le poids de la rareté " + rarityName + " ne peut pas être négatif : " + weight);
            }
        });
        moneyBonusRates.forEach((level, rate) -> {
            if (rate < 0) {
                throw new IllegalArgumentException("Le bonus d'argent du niveau " + level + " ne peut pas être négatif : " + rate);
            }
        });
        rarityWeights = Map.copyOf(rarityWeights);
        quantityByRarity = Map.copyOf(quantityByRarity);
        quantityRateByQuality = Map.copyOf(quantityRateByQuality);
        harvestTiers = Map.copyOf(harvestTiers);
        Map<CommissionBoardUpgradeType, Map<Integer, CommissionBoardUpgradeStep>> frozenSteps =
                new EnumMap<>(CommissionBoardUpgradeType.class);
        upgradeSteps.forEach((upgradeType, steps) -> frozenSteps.put(upgradeType, Map.copyOf(steps)));
        upgradeSteps = Map.copyOf(frozenSteps);
        moneyBonusRates = Map.copyOf(moneyBonusRates);
    }

    /** @return poids de tirage de cette rareté, zéro si elle n'est pas configurée : ses items ne sont pas tirés */
    public double getRarityWeight(String rarityName) {
        return rarityWeights.getOrDefault(rarityName, 0.0);
    }

    /** @return quantité de base et variance de cette rareté, {@link CommissionRarityQuantity#DEFAULT} si elle n'est pas configurée */
    public CommissionRarityQuantity getRarityQuantity(String rarityName) {
        return quantityByRarity.getOrDefault(rarityName, CommissionRarityQuantity.DEFAULT);
    }

    /** @return coefficient de quantité de cette qualité, {@link QuantityRate#NEUTRAL} si elle n'est pas configurée */
    public QuantityRate getQuantityRate(Quality minimalQuality) {
        return quantityRateByQuality.getOrDefault(minimalQuality, QuantityRate.NEUTRAL);
    }

    /** @return le palier de ce niveau, ou {@link Optional#empty()} s'il n'existe pas */
    public Optional<CommissionBoardUpgradeStep> findUpgradeStep(CommissionBoardUpgradeType upgradeType, int upgradeLevel) {
        return Optional.ofNullable(upgradeSteps.getOrDefault(upgradeType, Map.of()).get(upgradeLevel));
    }

    /** @return taux du bonus d'argent à ce niveau, zéro s'il n'est pas configuré */
    public double getMoneyBonusRate(int moneyBonusLevel) {
        return moneyBonusRates.getOrDefault(moneyBonusLevel, 0.0);
    }
}
