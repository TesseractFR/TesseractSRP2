package onl.tesseract.srp.commission.domain.port.serverside;

import onl.tesseract.srp.commission.domain.model.board.UpgradeStep;
import onl.tesseract.srp.commission.domain.model.board.UpgradeType;

import java.util.Optional;

/** Port d'accès aux paramètres de configuration des commissions. */
public interface CommissionConfigRepository {
    /** @return part de la valeur des items payée au spawn (0,5 pour la moitié) */
    double getSpawnRewardRate();

    /** @return taux du bonus d'argent à ce niveau d'amélioration, zéro au niveau 0 */
    double getMoneyBonusRate(int moneyBonusLevel);

    /** @return XP de guilde accordée par lys de récompense */
    double getGuildXpPerLys();

    /** @return taux de taxe d'usage d'un tableau de guilde à sa création */
    double getDefaultUsageTaxRate();

    /** @return nombre de commissions par type sur le tableau du spawn */
    int getSpawnSlotsPerType();

    /**
     * @param upgradeLevel niveau à atteindre ; pour la qualité, rang de la qualité visée
     * @return le palier, ou {@link Optional#empty()} si ce niveau n'existe pas : l'amélioration est au maximum
     */
    Optional<UpgradeStep> findUpgradeStep(UpgradeType upgradeType, int upgradeLevel);
}
