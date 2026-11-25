package onl.tesseract.srp.commission.domain.model.exception;

import lombok.Getter;
import onl.tesseract.srp.commission.domain.model.board.GuildId;

/** La banque de la guilde n'a pas de quoi payer l'amélioration. */
@Getter
public final class NotEnoughBankMoneyException extends CommissionException {
    private final GuildId guildId;
    private final int cost;

    public NotEnoughBankMoneyException(GuildId guildId, int cost) {
        super("La banque de la guilde " + guildId.value() + " ne peut pas payer " + cost + " lys");
        this.guildId = guildId;
        this.cost = cost;
    }
}
