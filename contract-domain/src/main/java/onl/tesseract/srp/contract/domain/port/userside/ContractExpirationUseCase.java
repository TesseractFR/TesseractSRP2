package onl.tesseract.srp.contract.domain.port.userside;

import onl.tesseract.srp.contract.domain.model.contract.ContractId;

/** Cas d'usage d'expiration des contrats et de leurs parchemins. */
public interface ContractExpirationUseCase {
    /**
     * Supprime les contrats expirés.
     *
     * @return nombre de contrats supprimés
     */
    int purgeExpired();

    /**
     * Indique si un parchemin doit rester dans l'inventaire de son porteur.
     *
     * @return {@code true} si son contrat existe encore et n'a pas expiré
     */
    boolean isScrollValid(ContractId contractId);
}
