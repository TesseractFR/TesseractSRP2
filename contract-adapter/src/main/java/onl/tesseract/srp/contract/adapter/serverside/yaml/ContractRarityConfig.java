package onl.tesseract.srp.contract.adapter.serverside.yaml;

/** Configuration de la génération du catalogue selon la rareté des matériaux. */
public interface ContractRarityConfig {
    /** @return poids de tirage des matériaux de cette rareté, zéro pour les exclure */
    double getRarityWeight(String rarityName);

    /** @return quantité demandée par contrat pour les matériaux de cette rareté */
    double getQuantityByRarity(String rarityName);
}
