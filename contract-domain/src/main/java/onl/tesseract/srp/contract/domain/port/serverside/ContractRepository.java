package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.contract.domain.model.contract.Contract;

import java.time.Instant;
import java.util.Optional;

/** Port de persistance des contrats. */
public interface ContractRepository {
    /** Enregistre un contrat et retourne sa version persistée. */
    Contract save(Contract contract);
    /** Recherche un contrat par son identifiant. */
    Optional<Contract> findById(long id);
    /** Supprime le contrat identifié. */
    void delete(long id);
    /** Supprime les contrats expirés à l'instant indiqué. */
    int deleteExpired(Instant now);

}
