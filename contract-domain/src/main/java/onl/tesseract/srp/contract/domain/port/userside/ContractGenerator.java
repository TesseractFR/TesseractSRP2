package onl.tesseract.srp.contract.domain.port.userside;

import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.contract.domain.model.contract.CatalogItem;
import onl.tesseract.srp.contract.domain.model.contract.ContractDraft;
import onl.tesseract.srp.contract.domain.port.serverside.ContractCatalogRepository;
import onl.tesseract.srp.contract.domain.port.serverside.ContractConfigRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

/** Génère des contrats à partir du catalogue et des paramètres du domaine. */
public class ContractGenerator {
    private final ContractConfigRepository contractConfigRepository;
    private final ContractCatalogRepository contractCatalogRepository;
    private final RandomGenerator random;
    private final Clock clock;

    /**
     * @param contractConfigRepository fournit la durée de validité des parchemins
     * @param contractCatalogRepository fournit les éléments du catalogue et leur valeur
     * @param random source d'aléa des tirages
     * @param clock horloge qui date l'expiration des contrats
     */
    public ContractGenerator(ContractConfigRepository contractConfigRepository,
                             ContractCatalogRepository contractCatalogRepository, RandomGenerator random, Clock clock) {
        this.contractConfigRepository = contractConfigRepository;
        this.contractCatalogRepository = contractCatalogRepository;
        this.random = random;
        this.clock = clock;
    }

    /**
     * Tire un élément du catalogue selon son poids et une qualité minimale au hasard.
     *
     * @return le contrat généré, ou {@link Optional#empty()} si le catalogue est vide
     */
    public Optional<ContractDraft> generate() {
        List<CatalogItem> catalogItems = contractCatalogRepository.findAllCatalogItems();
        if (catalogItems.isEmpty()) {
            return Optional.empty();
        }
        CatalogItem catalogItem = pickWeighted(catalogItems);
        Quality[] allQualities = Quality.values();
        Quality minimalQuality = allQualities[random.nextInt(allQualities.length)];
        return Optional.of(new ContractDraft(catalogItem.type(), catalogItem.client(), catalogItem.material(),
                catalogItem.quantity(), minimalQuality, computeReward(catalogItem, minimalQuality),
                clock.instant().plus(contractConfigRepository.getScrollLifetime())));
    }

    /** La récompense vaut la valeur des matériaux demandés, arrondie à l'unité, avec un minimum d'un lys. */
    private MoneyValue computeReward(CatalogItem catalogItem, Quality minimalQuality) {
        BigDecimal unitValue = contractCatalogRepository.getUnitValue(catalogItem.material(), minimalQuality).value();
        BigDecimal reward = unitValue.multiply(BigDecimal.valueOf(catalogItem.quantity()))
                .setScale(0, RoundingMode.HALF_UP)
                .max(BigDecimal.ONE);
        return new MoneyValue(reward);
    }

    /** Sélectionne un élément du catalogue proportionnellement à son poids. */
    private CatalogItem pickWeighted(List<CatalogItem> catalogItems) {
        double totalWeight = catalogItems.stream().mapToDouble(CatalogItem::weight).sum();
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
