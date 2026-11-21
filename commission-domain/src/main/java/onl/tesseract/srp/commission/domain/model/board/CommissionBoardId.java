package onl.tesseract.srp.commission.domain.model.board;

/** Identifiant d'un tableau de commissions enregistré. */
public record CommissionBoardId(long value) {
    /** Vérifie que l'identifiant est strictement positif, comme ceux attribués par le dépôt. */
    public CommissionBoardId {
        if (value <= 0) {
            throw new IllegalArgumentException("Un identifiant de tableau doit être strictement positif : " + value);
        }
    }
}
