package onl.tesseract.srp.commission.domain.model.board;

import java.util.List;

/**
 * État des conditions du prochain palier d'une amélioration, pour un tableau donné. C'est la seule
 * évaluation de ces conditions : le menu l'affiche et le service l'applique.
 *
 * @param step             le palier visé
 * @param hasEnoughMoney   {@code true} si la banque de la guilde peut payer le palier
 * @param hasBoardTier     {@code true} si le tableau a le tier demandé par le palier
 * @param deliveryProgress avancement sur chaque condition de livraison du palier, liste vide s'il n'en a pas
 */
public record CommissionBoardUpgradeConditions(CommissionBoardUpgradeStep step, boolean hasEnoughMoney, boolean hasBoardTier,
                                               List<CommissionBoardDeliveryProgress> deliveryProgress) {

    public CommissionBoardUpgradeConditions {
        deliveryProgress = List.copyOf(deliveryProgress);
    }

    /** @return {@code true} si toutes les conditions de livraison sont remplies */
    public boolean hasDeliveries() {
        return deliveryProgress.stream().allMatch(CommissionBoardDeliveryProgress::isMet);
    }

    /** @return {@code true} si le palier peut être acheté maintenant */
    public boolean areAllMet() {
        return hasEnoughMoney && hasBoardTier && hasDeliveries();
    }
}
