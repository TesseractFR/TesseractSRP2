package onl.tesseract.srp.commission.adapter.serverside.yaml;

import onl.tesseract.srp.common.domain.model.item.MaterialName;

import java.util.Map;

/** Partie de la configuration des commissions qui sert à construire le catalogue. */
public interface CommissionCatalogConfig {
    /** @return les items de récolte que les tableaux peuvent demander, avec leur tier ; table vide si aucun */
    Map<MaterialName, Integer> findAllHarvestTiers();

    /** @return quantité de base et variance d'un item de cette rareté, nulles si elle n'est pas configurée */
    CommissionRarityQuantity getRarityQuantity(String rarityName);

    /** @return poids de tirage d'un item de cette rareté, zéro si elle n'est pas configurée */
    double getRarityWeight(String rarityName);
}
