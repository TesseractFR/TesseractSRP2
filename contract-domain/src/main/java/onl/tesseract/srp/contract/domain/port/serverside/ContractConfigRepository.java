package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.contract.domain.model.contract.ContractQualityWeights;

import java.time.Duration;

/** Port d'accès aux paramètres de configuration des contrats. */
public interface ContractConfigRepository {
    /** @return durée de validité des parchemins nouvellement générés */
    Duration getScrollLifetime();

    /** @return poids de tirage de la qualité minimale des contrats générés */
    ContractQualityWeights getQualityWeights();
}
