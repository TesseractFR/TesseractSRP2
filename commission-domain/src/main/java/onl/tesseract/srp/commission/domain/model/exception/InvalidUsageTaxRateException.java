package onl.tesseract.srp.commission.domain.model.exception;

import lombok.Getter;
import onl.tesseract.srp.commission.domain.model.board.UsageTaxRate;

/** Le taux de taxe d'usage sort de l'intervalle autorisé. */
@Getter
public final class InvalidUsageTaxRateException extends CommissionException {
    private final int percent;

    public InvalidUsageTaxRateException(int percent) {
        super("Le taux de taxe d'usage doit être entre 0 et " + UsageTaxRate.MAX_PERCENT + " % : " + percent + " %");
        this.percent = percent;
    }
}
