package onl.tesseract.srp.contract.domain.model.contract;

/** Identifiant d'un contrat enregistré. */
public record ContractId(long value) {
    /** Vérifie que l'identifiant est strictement positif, comme ceux attribués par le dépôt. */
    public ContractId {
        if (value <= 0) {
            throw new IllegalArgumentException("Un identifiant de contrat doit être strictement positif : " + value);
        }
    }
}
