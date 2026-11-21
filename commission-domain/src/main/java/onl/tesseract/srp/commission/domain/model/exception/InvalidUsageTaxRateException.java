package onl.tesseract.srp.commission.domain.model.exception;

import lombok.Getter;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoard;

/** Le taux de taxe d'usage sort de l'intervalle autorisé. */
@Getter
public final class InvalidUsageTaxRateException extends CommissionException {
    private final double usageTaxRate;

    public InvalidUsageTaxRateException(double usageTaxRate) {
        super("Le taux de taxe d'usage doit être entre 0 et " + CommissionBoard.MAX_USAGE_TAX_RATE + " : " + usageTaxRate);
        this.usageTaxRate = usageTaxRate;
    }
}
