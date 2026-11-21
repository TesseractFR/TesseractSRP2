package onl.tesseract.srp.commission.domain.model.commission;

import lombok.NonNull;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardId;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardPart;
import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;

/**
 * Commission générée mais pas encore enregistrée. Elle n'a ni identifiant, ni livraison : c'est le
 * dépôt qui l'attribue en la transformant en {@link Commission}.
 */
public record CommissionDraft(@NonNull CommissionBoardId boardId, @NonNull CommissionBoardPart part, @NonNull CommissionType type,
                              @NonNull MaterialName material, int quantity, @NonNull Quality minimalQuality,
                              @NonNull MoneyValue reward) {
    /** Vérifie que la quantité demandée vaut au moins un. */
    public CommissionDraft {
        if (quantity < 1) {
            throw new IllegalArgumentException("La quantité d'une commission vaut au moins 1 : " + quantity);
        }
    }
}
