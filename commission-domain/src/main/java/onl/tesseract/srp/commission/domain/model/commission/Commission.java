package onl.tesseract.srp.commission.domain.model.commission;

import lombok.NonNull;
import onl.tesseract.srp.commission.domain.model.board.BoardId;
import onl.tesseract.srp.commission.domain.model.board.BoardPart;
import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;

/**
 * Commission d'un tableau. Plusieurs joueurs y contribuent jusqu'à ce que {@code quantity} items soient livrés.
 *
 * @param deliveredQuantity quantité déjà livrée, entre zéro et {@code quantity}
 * @param reward            récompense totale, figée à la génération
 */
public record Commission(@NonNull CommissionId id, @NonNull BoardId boardId, @NonNull BoardPart part,
                         @NonNull CommissionType type, @NonNull MaterialName material, int quantity,
                         int deliveredQuantity, @NonNull Quality minimalQuality, @NonNull MoneyValue reward) {

    /** Vérifie que la quantité vaut au moins un et que la quantité livrée ne la dépasse pas. */
    public Commission {
        if (quantity < 1) {
            throw new IllegalArgumentException("La quantité d'une commission vaut au moins 1 : " + quantity);
        }
        if (deliveredQuantity < 0 || deliveredQuantity > quantity) {
            throw new IllegalArgumentException("La quantité livrée doit être entre 0 et " + quantity + " : " + deliveredQuantity);
        }
    }

    /** @return quantité qu'il reste à livrer */
    public int getRemainingQuantity() {
        return quantity - deliveredQuantity;
    }

    /** @return {@code true} quand toute la quantité est livrée */
    public boolean isCompleted() {
        return getRemainingQuantity() == 0;
    }

    /** @return {@code true} si la commission est dans la liste de cette partie et de ce type */
    public boolean isInList(BoardPart listPart, CommissionType listType) {
        return part == listPart && type == listType;
    }

    /** @return part de la récompense rapportée par les {@code itemCount} prochains items */
    public int computeRewardShareFor(int itemCount) {
        return computeShareFor(reward.value().intValueExact(), itemCount);
    }

    /**
     * La part est calculée à partir de la progression totale, si bien que la somme des parts vaut
     * exactement {@code total}, quel que soit le découpage des livraisons.
     *
     * @return part de {@code total} rapportée par les {@code itemCount} prochains items
     */
    public int computeShareFor(int total, int itemCount) {
        return computeTotalShareAfter(total, deliveredQuantity + itemCount) - computeTotalShareAfter(total, deliveredQuantity);
    }

    private int computeTotalShareAfter(int total, int totalDeliveredQuantity) {
        return (int) Math.round((double) total * totalDeliveredQuantity / quantity);
    }
}
