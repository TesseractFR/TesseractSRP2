package onl.tesseract.srp.contract.domain.port.userside;

import onl.tesseract.srp.contract.domain.model.contract.ContractId;
import onl.tesseract.srp.contract.domain.port.serverside.ContractRepository;

import java.time.Clock;

/** Gère l'expiration des contrats et la validité de leurs parchemins. */
public class ContractExpirationService {

    private final ContractRepository contractRepository;
    private final Clock clock;

    public ContractExpirationService(ContractRepository contractRepository, Clock clock) {
        this.contractRepository = contractRepository;
        this.clock = clock;
    }

    /**
     * Supprime les contrats expirés.
     *
     * @return nombre de contrats supprimés
     */
    public int purgeExpired() {
        return contractRepository.deleteExpired(clock.instant());
    }

    /**
     * Indique si un parchemin doit rester dans l'inventaire de son porteur.
     *
     * @return {@code true} si son contrat existe encore et n'a pas expiré
     */
    public boolean isScrollValid(ContractId contractId) {
        return contractRepository.findById(contractId)
                .map(contract -> !contract.isExpiredAt(clock.instant()))
                .orElse(false);
    }
}
