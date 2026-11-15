package onl.tesseract.srp.contract.domain.model.exception;

import onl.tesseract.srp.contract.domain.model.contract.ContractId;
import onl.tesseract.srp.contract.domain.model.contract.ContractClient;

/** Le contrat est livré à un autre destinataire que le sien. */
public final class WrongContractClientException extends ContractException {
    private final ContractId contractId;
    private final ContractClient expectedClient;
    private final ContractClient actualClient;

    public WrongContractClientException(ContractId contractId, ContractClient expectedClient, ContractClient actualClient) {
        super("Le contrat " + contractId.value() + " est destiné à " + expectedClient.name() + ", pas à " + actualClient.name());
        this.contractId = contractId;
        this.expectedClient = expectedClient;
        this.actualClient = actualClient;
    }

    public ContractId getContractId() {
        return contractId;
    }

    public ContractClient getExpectedClient() {
        return expectedClient;
    }

    public ContractClient getActualClient() {
        return actualClient;
    }
}
