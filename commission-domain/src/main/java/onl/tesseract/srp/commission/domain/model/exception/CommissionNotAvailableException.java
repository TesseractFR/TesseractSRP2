package onl.tesseract.srp.commission.domain.model.exception;

import lombok.Getter;
import onl.tesseract.srp.commission.domain.model.commission.CommissionId;

/** La commission n'existe plus ou est déjà terminée. */
@Getter
public final class CommissionNotAvailableException extends CommissionException {
    private final CommissionId commissionId;

    public CommissionNotAvailableException(CommissionId commissionId) {
        super("La commission " + commissionId.value() + " n'est plus disponible");
        this.commissionId = commissionId;
    }
}
