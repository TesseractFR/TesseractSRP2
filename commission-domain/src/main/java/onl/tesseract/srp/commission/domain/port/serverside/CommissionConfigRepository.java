package onl.tesseract.srp.commission.domain.port.serverside;

import onl.tesseract.srp.commission.domain.model.board.CommissionBoardUpgradeStep;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardUpgradeType;
import onl.tesseract.srp.commission.domain.model.board.UsageTaxRate;
import onl.tesseract.srp.commission.domain.model.commission.CommissionQualityWeights;
import onl.tesseract.srp.commission.domain.model.commission.QuantityRate;
import onl.tesseract.srp.common.domain.model.enums.Quality;

import java.util.Optional;

/** Port d'accès aux paramètres de configuration des commissions. */
public interface CommissionConfigRepository {
    /** @return part de la valeur des items payée au spawn (0,5 pour la moitié) */
    double getSpawnRewardRate();

    /** @return taux du bonus d'argent à ce niveau d'amélioration, zéro au niveau 0 */
    double getMoneyBonusRate(int moneyBonusLevel);

    /** @return XP de guilde accordée par lys de récompense */
    double getGuildXpPerLys();

    /** @return taxe d'usage d'un tableau de guilde à sa création */
    UsageTaxRate getDefaultUsageTaxRate();

    /** @return poids de tirage de la qualité minimale des commissions générées */
    CommissionQualityWeights getQualityWeights();

    /** @return coefficient appliqué à la quantité tirée selon la qualité minimale, neutre si elle n'est pas configurée */
    QuantityRate getQuantityRate(Quality minimalQuality);

    /** @return nombre de commissions par type sur le tableau du spawn */
    int getSpawnSlotsPerType();

    /**
     * @param upgradeLevel niveau à atteindre
     * @return le palier, ou {@link Optional#empty()} si ce niveau n'existe pas : l'amélioration est au maximum
     */
    Optional<CommissionBoardUpgradeStep> findUpgradeStep(CommissionBoardUpgradeType upgradeType, int upgradeLevel);
}
