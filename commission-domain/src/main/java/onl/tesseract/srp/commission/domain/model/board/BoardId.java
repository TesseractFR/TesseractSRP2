package onl.tesseract.srp.commission.domain.model.board;

/** Identifiant d'un tableau de commissions enregistré. */
public record BoardId(long value) {
    /** Vérifie que l'identifiant est strictement positif, comme ceux attribués par le dépôt. */
    public BoardId {
        if (value <= 0) {
            throw new IllegalArgumentException("Un identifiant de tableau doit être strictement positif : " + value);
        }
    }
}
