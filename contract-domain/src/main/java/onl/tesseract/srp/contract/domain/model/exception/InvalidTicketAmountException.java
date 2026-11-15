package onl.tesseract.srp.contract.domain.model.exception;

/** Le nombre de tickets à attribuer n'est pas strictement positif. */
public final class InvalidTicketAmountException extends ContractException {
    private final int amount;

    public InvalidTicketAmountException(int amount) {
        super("Le nombre de tickets doit être strictement positif : " + amount);
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}
