package onl.tesseract.srp.commission.domain.model.commission;

import onl.tesseract.srp.commission.domain.model.board.CommissionBoardId;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardPart;
import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;

/**
 * Commission générée mais pas encore enregistrée. Elle n'a ni identifiant, ni livraison : c'est le
 * dépôt qui l'attribue en la transformant en {@link Commission}.
 */
public record CommissionDraft(CommissionBoardId boardId, CommissionBoardPart part, CommissionType type,
                              MaterialName material, int quantity, Quality minimalQuality,
                              MoneyValue reward) {
}
