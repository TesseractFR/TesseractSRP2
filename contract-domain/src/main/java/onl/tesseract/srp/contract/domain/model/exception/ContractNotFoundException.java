package onl.tesseract.srp.contract.domain.model.exception;

import onl.tesseract.srp.contract.domain.model.contract.ContractId;

/** Le parchemin présenté ne correspond à aucun contrat enregistré. */
public final class ContractNotFoundException extends ContractException {
    private final ContractId contractId;

    public ContractNotFoundException(ContractId contractId) {
        super("Contrat introuvable : " + contractId.value());
        this.contractId = contractId;
    }

    public ContractId getContractId() {
        return contractId;
    }
}
