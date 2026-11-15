package onl.tesseract.srp.contract.domain.port.serverside;

import java.time.Duration;

/** Port d'accès aux paramètres de configuration des contrats. */
public interface ContractConfigRepository {
    /** @return durée de validité des parchemins nouvellement générés */
    Duration getScrollLifetime();

    /**
     * @return écart maximal, en pourcentage, entre la quantité d'un contrat et la quantité de base
     *         de son élément de catalogue (50 : une base de 50 donne une quantité entre 25 et 75)
     */
    double getQuantityVariancePercent();
}
