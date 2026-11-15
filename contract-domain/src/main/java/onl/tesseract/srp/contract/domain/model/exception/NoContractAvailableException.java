package onl.tesseract.srp.contract.domain.model.exception;

import onl.tesseract.srp.common.domain.model.PlayerId;

/** Le catalogue ne permet de générer aucun contrat. */
public final class NoContractAvailableException extends ContractException {
    private final PlayerId buyer;

    public NoContractAvailableException(PlayerId buyer) {
        super("Aucun contrat disponible pour le joueur " + buyer.value());
        this.buyer = buyer;
    }

    public PlayerId getBuyer() {
        return buyer;
    }
}
