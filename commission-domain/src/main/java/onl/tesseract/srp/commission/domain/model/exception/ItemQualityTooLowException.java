package onl.tesseract.srp.commission.domain.model.exception;

import lombok.Getter;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.commission.domain.model.commission.CommissionId;
import onl.tesseract.srp.common.domain.model.enums.Quality;

/** Le joueur possède l'item demandé, mais seulement sous la qualité minimale. */
@Getter
public final class ItemQualityTooLowException extends CommissionException {
    private final PlayerId player;
    private final CommissionId commissionId;
    private final Quality minimalQuality;

    public ItemQualityTooLowException(PlayerId player, CommissionId commissionId, Quality minimalQuality) {
        super("Les items du joueur " + player.value() + " sont sous la qualité " + minimalQuality + " demandée par la commission " + commissionId.value());
        this.player = player;
        this.commissionId = commissionId;
        this.minimalQuality = minimalQuality;
    }
}
