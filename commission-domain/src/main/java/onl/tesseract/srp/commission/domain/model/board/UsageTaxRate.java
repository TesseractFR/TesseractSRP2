package onl.tesseract.srp.commission.domain.model.board;

import onl.tesseract.srp.commission.domain.model.exception.InvalidUsageTaxRateException;

/**
 * Taxe d'usage d'un tableau, payée par les joueurs hors de la guilde propriétaire et versée à sa banque.
 * Elle est exprimée en pourcentage entier, pour que les paliers affichés et appliqués soient exacts.
 *
 * @param percent taux en pourcentage, entre 0 et {@link #MAX_PERCENT}
 */
public record UsageTaxRate(int percent) {

    /** Taux maximal qu'une guilde peut fixer. */
    public static final int MAX_PERCENT = 50;

    /** Aucune taxe : celle du tableau du spawn. */
    public static final UsageTaxRate ZERO = new UsageTaxRate(0);

    /** @throws InvalidUsageTaxRateException si le taux sort de l'intervalle [0, {@link #MAX_PERCENT}] */
    public UsageTaxRate {
        if (percent < 0 || percent > MAX_PERCENT) {
            throw new InvalidUsageTaxRateException(percent);
        }
    }

    /** La taxe est arrondie à l'unité inférieure : le joueur n'est jamais taxé plus que le taux affiché. */
    public int computeTaxOn(int amount) {
        return amount * percent / 100;
    }
}
