package onl.tesseract.srp.contract.domain.port.serverside;

import java.time.Duration;

/** Port d'accès aux paramètres de configuration des contrats. */
public interface ContractConfigRepository {
    /** @return durée de validité des parchemins nouvellement générés */
    Duration getScrollTtl();
}
