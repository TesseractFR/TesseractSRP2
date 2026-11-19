package onl.tesseract.srp.contract.domain.model.exception;

import onl.tesseract.srp.contract.domain.model.contract.ContractId;

/** Le contrat livré a dépassé sa date d'expiration. */
public final class ContractExpiredException extends ContractException {
    private final ContractId contractId;

    public ContractExpiredException(ContractId contractId) {
        super("Contrat expiré : " + contractId.value());
        this.contractId = contractId;
    }

    public ContractId getContractId() {
        return contractId;
    }
}
