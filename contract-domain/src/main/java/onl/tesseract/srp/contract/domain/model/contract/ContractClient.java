package onl.tesseract.srp.contract.domain.model.contract;

/**
 * Destinataire d'un contrat : le métier (récolte) ou la compétence (artisanat) dont le PNJ
 * accepte la livraison.
 */
public record ContractClient(String name) {
    /** Vérifie que le nom est renseigné. */
    public ContractClient {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le destinataire d'un contrat doit avoir un nom");
        }
    }
}
