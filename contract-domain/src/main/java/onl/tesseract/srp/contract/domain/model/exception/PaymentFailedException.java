package onl.tesseract.srp.contract.domain.model.exception;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.MoneyValue;

/** Le versement d'une récompense au joueur a échoué. */
public final class PaymentFailedException extends ContractException {
    private final PlayerId player;
    private final MoneyValue amount;

    public PaymentFailedException(PlayerId player, MoneyValue amount) {
        super("Le versement de " + amount.value() + " au joueur " + player.value() + " a échoué");
        this.player = player;
        this.amount = amount;
    }

    public PlayerId getPlayer() {
        return player;
    }

    public MoneyValue getAmount() {
        return amount;
    }
}
