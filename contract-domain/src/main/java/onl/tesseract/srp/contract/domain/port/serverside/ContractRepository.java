package onl.tesseract.srp.contract.domain.port.serverside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.contract.domain.model.contract.Contract;

import java.time.Instant;
import java.util.Optional;

/** Port de persistance et de règlement des contrats. */
public interface ContractRepository {
    /** Enregistre un contrat et retourne sa version persistée. */
    Contract save(Contract contract);
    /** Recherche un contrat par son identifiant. */
    Optional<Contract> findById(long id);
    /** Supprime le contrat identifié. */
    void delete(long id);
    /** Supprime les contrats expirés à l'instant indiqué. */
    int deleteExpired(Instant now);

    /**
     * Tente de régler un contrat et de le marquer comme traité.
     *
     * @return {@code true} si le règlement a été effectué, {@code false} si le contrat ne peut plus être réglé
     * @throws onl.tesseract.srp.contract.domain.model.exception.PaymentFailedException si le paiement échoue
     */
    boolean settle(long contractId, PlayerId deliverer, int payout);

}
