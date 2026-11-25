package onl.tesseract.srp.commission.domain.model.board;

/**
 * Avancement d'un tableau sur une condition de livraison d'un palier.
 *
 * @param deliveredItemCount items déjà livrés qui comptent pour cette condition
 */
public record CommissionBoardDeliveryProgress(CommissionBoardDeliveryRequirement requirement, int deliveredItemCount) {

    /** @return {@code true} si le tableau a reçu assez d'items pour cette condition */
    public boolean isMet() {
        return deliveredItemCount >= requirement.itemCount();
    }
}
