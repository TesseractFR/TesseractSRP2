package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.model.contract.ContractDraft;
import onl.tesseract.srp.contract.domain.model.contract.ContractId;

import java.time.Instant;
import java.util.Optional;

/** Port de persistance des contrats. */
public interface ContractRepository {
    /**
     * Enregistre un contrat généré et lui attribue un identifiant.
     *
     * @param draft contrat à enregistrer
     * @return le contrat enregistré, avec son identifiant
     */
    Contract create(ContractDraft draft);

    /**
     * Recherche un contrat par son identifiant.
     *
     * @return le contrat, ou {@link Optional#empty()} s'il n'existe pas ou a été supprimé
     */
    Optional<Contract> findById(ContractId id);

    /** Supprime le contrat identifié. Ne fait rien s'il n'existe pas. */
    void delete(ContractId id);

    /**
     * Supprime les contrats dont la date d'expiration est atteinte à l'instant indiqué.
     *
     * @return nombre de contrats supprimés
     */
    int deleteExpired(Instant now);
}
