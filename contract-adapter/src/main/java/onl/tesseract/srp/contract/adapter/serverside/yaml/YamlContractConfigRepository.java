package onl.tesseract.srp.contract.adapter.serverside.yaml;

import onl.tesseract.srp.contract.domain.port.serverside.ContractConfigRepository;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

@Component
public class YamlContractConfigRepository implements ContractConfigRepository {

    private static final Path FILE = Path.of("plugins/Tesseract/contract/contracts.yml");

    private YamlConfiguration yaml = new YamlConfiguration();
    private long loadedAt = -1;

    @Override
    public Duration getScrollTtl() {
        return Duration.ofHours(yaml().getLong("scroll-lifetime-hours", 72));
    }

    public double getQuantityByRarity(String rarity) {
        return yaml().getDouble("quantity-by-rarity." + rarity, 300);
    }

    public double getRarityWeight(String rarity) {
        return yaml().getDouble("rarity-weights." + rarity, 0);
    }

    public Optional<String> getNpcName(String job) {
        ConfigurationSection names = yaml().getConfigurationSection("npc-names");
        if (names == null) {
            return Optional.empty();
        }
        for (String key : names.getKeys(false)) {
            if (key.equalsIgnoreCase(job)) {
                return Optional.ofNullable(names.getString(key)).filter(name -> !name.isBlank());
            }
        }
        return Optional.empty();
    }

    private synchronized YamlConfiguration yaml() {
        File file = FILE.toFile();
        if (!file.exists()) {
            return new YamlConfiguration();
        }
        long modified = file.lastModified();
        if (modified != loadedAt) {
            yaml = YamlConfiguration.loadConfiguration(file);
            loadedAt = modified;
        }
        return yaml;
    }
}