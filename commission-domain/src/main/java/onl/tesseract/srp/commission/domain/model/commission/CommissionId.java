package onl.tesseract.srp.commission.domain.model.commission;

/** Identifiant d'une commission enregistrée. */
public record CommissionId(long value) {
    /** Vérifie que l'identifiant est strictement positif, comme ceux attribués par le dépôt. */
    public CommissionId {
        if (value <= 0) {
            throw new IllegalArgumentException("Un identifiant de commission doit être strictement positif : " + value);
        }
    }
}
