package onl.tesseract.srp.commission.domain.model.board;

import onl.tesseract.srp.commission.domain.model.commission.CommissionType;
import onl.tesseract.srp.common.domain.model.enums.Quality;

/**
 * Condition d'un palier du tier du tableau : avoir livré assez d'items sur des commissions de ce type
 * qui demandent au moins cette qualité.
 *
 * @param minimalQuality qualité demandée par les commissions comptées, ou une qualité supérieure
 * @param itemCount      nombre d'items à avoir livrés
 */
public record CommissionBoardDeliveryRequirement(CommissionType type, Quality minimalQuality, int itemCount) {
    public CommissionBoardDeliveryRequirement {
        if (itemCount < 1) {
            throw new IllegalArgumentException("Une condition de livraison demande au moins un item : " + itemCount);
        }
    }
}
