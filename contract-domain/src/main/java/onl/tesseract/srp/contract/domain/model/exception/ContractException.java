package onl.tesseract.srp.contract.domain.model.exception;

/**
 * Racine des refus métier du module contrat. La hiérarchie est scellée pour que les adapters
 * qui traduisent ces exceptions en message soient forcés par le compilateur de traiter chaque cas.
 */
public abstract sealed class ContractException extends RuntimeException
        permits NoContractTicketLeftException, ScrollInventoryFullException, NoContractAvailableException,
        ContractNotFoundException, ContractExpiredException, WrongContractClientException,
        ContractScrollNotOwnedException, MissingContractItemsException, InvalidTicketAmountException,
        PaymentFailedException, NotEnoughMaterialException {

    protected ContractException(String message) {
        super(message);
    }
}
