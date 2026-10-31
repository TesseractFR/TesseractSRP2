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
    private final ContractConfigRepository config;
    private final ItemInfoRepository itemInfo;
    private final Random random;

    public ContractGenerator(ContractConfigRepository config, ItemInfoRepository itemInfo, Random random) {
        this.config = config;
        this.itemInfo = itemInfo;
        this.random = random;
    }

    public Optional<Contract> generate(PlayerID buyer) {
        List<CatalogItem> items = itemInfo.getCatalogItems();
        if (items.isEmpty()) {
            return Optional.empty();
        }
        CatalogItem item = pick(items);
        Quality[] qualities = Quality.values();
        Quality minimal = qualities[random.nextInt(qualities.length)];
        Instant now = Instant.now();
        return Optional.of(new Contract(null, buyer, item.type(), item.job(), item.material(), item.quantity(), minimal,
                reward(item, minimal), now, now.plus(config.getScrollTtl())));
    }

    private int reward(CatalogItem item, Quality minimal) {
        double value = itemInfo.getValue(item.material(), minimal);
        return (int) Math.max(1, Math.round(value * item.quantity()));
    }

    private CatalogItem pick(List<CatalogItem> items) {
        double total = 0;
        for (CatalogItem item : items) {
            total += item.weight();
        }
        double r = random.nextDouble() * total;
        for (CatalogItem item : items) {
            r -= item.weight();
            if (r < 0) {
                return item;
            }
        }
        return items.getLast();
    }
}