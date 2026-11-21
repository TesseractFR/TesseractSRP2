package onl.tesseract.srp.commission.domain.model.board;

import lombok.NonNull;

import java.util.List;

/**
 * Palier d'une amélioration.
 *
 * @param cost                 prix en lys, payé par la banque de la guilde
 * @param requiredBoardTier    tier du tableau minimal ; 1 pour le tier du tableau lui-même
 * @param deliveryRequirements livraisons à avoir faites, pour le tier du tableau ; liste vide pour les autres
 */
public record CommissionBoardUpgradeStep(int cost, int requiredBoardTier, @NonNull List<CommissionBoardDeliveryRequirement> deliveryRequirements) {
    /** Vérifie que le prix n'est pas négatif et que le tier vaut au moins un, et fige la liste. */
    public CommissionBoardUpgradeStep {
        if (cost < 0) {
            throw new IllegalArgumentException("Le prix d'une amélioration ne peut pas être négatif : " + cost);
        }
        if (requiredBoardTier < 1) {
            throw new IllegalArgumentException("Le tier de tableau requis vaut au moins 1 : " + requiredBoardTier);
        }
        deliveryRequirements = List.copyOf(deliveryRequirements);
    }
}
