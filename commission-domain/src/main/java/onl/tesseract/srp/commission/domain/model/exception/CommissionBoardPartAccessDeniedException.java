package onl.tesseract.srp.commission.domain.model.exception;

import lombok.Getter;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.commission.domain.model.commission.CommissionId;

/** Le joueur n'a pas accès à la partie du tableau où se trouve la commission. */
@Getter
public final class CommissionBoardPartAccessDeniedException extends CommissionException {
    private final PlayerId player;
    private final CommissionId commissionId;

    public CommissionBoardPartAccessDeniedException(PlayerId player, CommissionId commissionId) {
        super("Le joueur " + player.value() + " n'a pas accès à la commission " + commissionId.value());
        this.player = player;
        this.commissionId = commissionId;
    }
}
