package onl.tesseract.srp.commission.domain.model.exception;

import lombok.Getter;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardId;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardDeliveryRequirement;

/** Le tableau n'a pas encore reçu assez de livraisons pour monter de tier. */
@Getter
public final class NotEnoughDeliveredItemsException extends CommissionException {
    private final CommissionBoardId boardId;
    private final CommissionBoardDeliveryRequirement requirement;
    private final int deliveredItemCount;

    public NotEnoughDeliveredItemsException(CommissionBoardId boardId, CommissionBoardDeliveryRequirement requirement, int deliveredItemCount) {
        super("Le tableau " + boardId.value() + " a reçu " + deliveredItemCount + " items " + requirement.type()
                + " de qualité " + requirement.minimalQuality() + " ou mieux, il en faut " + requirement.itemCount());
        this.boardId = boardId;
        this.requirement = requirement;
        this.deliveredItemCount = deliveredItemCount;
    }
}
