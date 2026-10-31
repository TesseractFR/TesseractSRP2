package onl.tesseract.srp.contract.domain.port.userside;

import onl.tesseract.srp.contract.domain.model.PlayerID;
import onl.tesseract.srp.contract.domain.model.Quality;
import onl.tesseract.srp.contract.domain.model.contract.CatalogItem;
import onl.tesseract.srp.contract.domain.model.contract.Contract;
import onl.tesseract.srp.contract.domain.port.serverside.ContractConfigRepository;
import onl.tesseract.srp.contract.domain.port.serverside.ItemInfoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class ContractGenerator {
    private final ContractConfigRepository contractConfigRepository;
    private final ItemInfoRepository itemInfoRepository;
    private final Random random;

    public ContractGenerator(ContractConfigRepository contractConfigRepository, ItemInfoRepository itemInfoRepository, Random random) {
        this.contractConfigRepository = contractConfigRepository;
        this.itemInfoRepository = itemInfoRepository;
        this.random = random;
    }

    public Optional<Contract> generate(PlayerID buyer) {
        List<CatalogItem> catalogItems = itemInfoRepository.getCatalogItems();
        if (catalogItems.isEmpty()) {
            return Optional.empty();
        }
        CatalogItem catalogItem = pickWeighted(catalogItems);
        Quality[] allQualities = Quality.values();
        Quality minimalQuality = allQualities[random.nextInt(allQualities.length)];
        Instant issuedAt = Instant.now();
        return Optional.of(new Contract(null, buyer, catalogItem.type(), catalogItem.job(), catalogItem.material(), catalogItem.quantity(), minimalQuality,
                reward(catalogItem, minimalQuality), issuedAt, issuedAt.plus(contractConfigRepository.getScrollTtl())));
    }

    private int reward(CatalogItem catalogItem, Quality minimalQuality) {
        double itemValue = itemInfoRepository.getValue(catalogItem.material(), minimalQuality);
        return (int) Math.max(1, Math.round(itemValue * catalogItem.quantity()));
    }

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