package onl.tesseract.srp.commission.domain.model.commission;

import onl.tesseract.srp.commission.domain.model.board.CommissionBoardId;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardPart;
import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;

/**
 * Commission d'un tableau. Plusieurs joueurs y contribuent jusqu'à ce que {@code quantity} items soient livrés.
 *
 * @param deliveredQuantity quantité déjà livrée, entre zéro et {@code quantity}
 * @param reward            récompense totale, figée à la génération
 */
public record Commission(CommissionId id, CommissionBoardId boardId, CommissionBoardPart part,
                         CommissionType type, MaterialName material, int quantity,
                         int deliveredQuantity, Quality minimalQuality, MoneyValue reward) {

    /** @return quantité qu'il reste à livrer */
    public int getRemainingQuantity() {
        return quantity - deliveredQuantity;
    }

    /** @return {@code true} quand toute la quantité est livrée */
    public boolean isCompleted() {
        return getRemainingQuantity() == 0;
    }

    /** @return {@code true} si la commission est dans cette partie du tableau et de ce type */
    public boolean belongsTo(CommissionBoardPart boardPart, CommissionType commissionType) {
        return part == boardPart && type == commissionType;
    }

    /** @return part de la récompense rapportée par les {@code itemCount} prochains items */
    public int computeRewardShareFor(int itemCount) {
        return computeProgressShareOf(reward.value().intValueExact(), itemCount);
    }

    /**
     * La part est calculée à partir de la progression totale, si bien que la somme des parts vaut
     * exactement {@code total}, quel que soit le découpage des livraisons.
     *
     * @param total montant réparti sur toute la commission : sa récompense, ou l'XP qu'elle donne à la guilde
     * @return part de {@code total} rapportée par les {@code itemCount} prochains items
     */
    public int computeProgressShareOf(int total, int itemCount) {
        return computeTotalShareAfter(total, deliveredQuantity + itemCount) - computeTotalShareAfter(total, deliveredQuantity);
    }

    private int computeTotalShareAfter(int total, int totalDeliveredQuantity) {
        return (int) Math.round((double) total * totalDeliveredQuantity / quantity);
    }
}
