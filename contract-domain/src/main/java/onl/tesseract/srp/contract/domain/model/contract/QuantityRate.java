package onl.tesseract.srp.contract.domain.model.contract;

/**
 * Coefficient appliqué à la quantité tirée d'un contrat selon sa qualité minimale : plus la qualité
 * demandée est haute, plus la quantité baisse.
 *
 * @param value coefficient multiplicateur, strictement positif
 */
public record QuantityRate(double value) {

    /** Coefficient neutre : la quantité reste inchangée. */
    public static final QuantityRate NEUTRAL = new QuantityRate(1);

    /** Vérifie que le coefficient est strictement positif. */
    public QuantityRate {
        if (!(value > 0)) {
            throw new IllegalArgumentException("Le coefficient de quantité doit être strictement positif : " + value);
        }
    }

    /**
     * Applique le coefficient à une quantité.
     *
     * @return {@code quantité × coefficient}, arrondi à l'unité la plus proche, avec un minimum de un
     */
    public int applyTo(int quantity) {
        return (int) Math.max(1, Math.round(quantity * value));
    }
}
