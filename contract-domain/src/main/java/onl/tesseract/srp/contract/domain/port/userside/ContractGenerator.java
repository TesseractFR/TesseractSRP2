package onl.tesseract.srp.contract.domain.port.userside;

import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.contract.domain.model.contract.CatalogItem;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.port.serverside.ContractConfigRepository;
import onl.tesseract.srp.contract.domain.port.serverside.ItemInfoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/** Génère des contrats à partir du catalogue et des paramètres du domaine. */
public class ContractGenerator {
    private final ContractConfigRepository contractConfigRepository;
    private final ItemInfoRepository itemInfoRepository;
    private final Random random;

    /**
     * Crée le générateur avec ses sources de configuration, de catalogue et d'aléa.
     * @param contractConfigRepository fournit la durée de validité des parchemins
     * @param itemInfoRepository fournit les éléments du catalogue et leur valeur
     * @param random générateur de nombres aléatoires utilisé pour les tirages
     */
    public ContractGenerator(ContractConfigRepository contractConfigRepository, ItemInfoRepository itemInfoRepository, Random random) {
        this.contractConfigRepository = contractConfigRepository;
        this.itemInfoRepository = itemInfoRepository;
        this.random = random;
    }

    /**
     * Tire un élément du catalogue selon son poids et crée un contrat pour l'acheteur.
     * @param buyer joueur auquel le contrat sera attribué
     * @return le contrat généré, ou un résultat vide si le catalogue est vide
     */
    public Optional<Contract> generate(PlayerId buyer) {
        List<CatalogItem> catalogItems = itemInfoRepository.getCatalogItems();
        if (catalogItems.isEmpty()) {
            return Optional.empty();
        }
        CatalogItem catalogItem = pickWeighted(catalogItems);
        Quality[] allQualities = Quality.values();
        Quality minimalQuality = allQualities[random.nextInt(allQualities.length)];
        return Optional.of(new Contract(null, catalogItem.type(), catalogItem.job(), catalogItem.material(), catalogItem.quantity(), minimalQuality,
                reward(catalogItem, minimalQuality), Instant.now().plus(contractConfigRepository.getScrollTtl())));
    }

    /** Calcule la récompense à partir de la valeur unitaire et de la quantité. */
    private int reward(CatalogItem catalogItem, Quality minimalQuality) {
        double itemValue = itemInfoRepository.getValue(catalogItem.material(), minimalQuality);
        return (int) Math.max(1, Math.round(itemValue * catalogItem.quantity()));
    }

    /** Sélectionne un élément du catalogue proportionnellement à son poids. */
    private CatalogItem pickWeighted(List<CatalogItem> catalogItems) {
        double totalWeight = 0;
        for (CatalogItem catalogItem : catalogItems) {
            totalWeight += catalogItem.weight();
        }
        double remainingWeight = random.nextDouble() * totalWeight;
        for (CatalogItem catalogItem : catalogItems) {
            remainingWeight -= catalogItem.weight();
            if (remainingWeight < 0) {
                return catalogItem;
            }
        }
        return catalogItems.getLast();
    }
}
