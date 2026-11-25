package onl.tesseract.srp.commission.domain.model.exception;

import lombok.Getter;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.commission.domain.model.commission.CommissionId;

/** Le joueur ne possède aucun item demandé par la commission. */
@Getter
public final class NoItemToDeliverException extends CommissionException {
    private final PlayerId player;
    private final CommissionId commissionId;

    public NoItemToDeliverException(PlayerId player, CommissionId commissionId) {
        super("Le joueur " + player.value() + " n'a aucun item à livrer pour la commission " + commissionId.value());
        this.player = player;
        this.commissionId = commissionId;
    }
}
