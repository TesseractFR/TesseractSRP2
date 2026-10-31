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

    private static final Path CONFIG_FILE_PATH = Path.of("plugins/Tesseract/contract/contracts.yml");

    private YamlConfiguration yamlConfiguration = new YamlConfiguration();
    private long loadedAt = -1;

    @Override
    public Duration getScrollTtl() {
        return Duration.ofHours(currentYaml().getLong("scroll-lifetime-hours", 72));
    }

    public double getQuantityByRarity(String rarity) {
        return currentYaml().getDouble("quantity-by-rarity." + rarity, 300);
    }

    public double getRarityWeight(String rarity) {
        return currentYaml().getDouble("rarity-weights." + rarity, 0);
    }

    public int getDailyTickets(String rank) {
        return currentYaml().getInt("daily-tickets." + rank, 0);
    }

    public Optional<String> getNpcName(String job) {
        ConfigurationSection npcNames = currentYaml().getConfigurationSection("npc-names");
        if (npcNames == null) {
            return Optional.empty();
        }
        for (String key : npcNames.getKeys(false)) {
            if (key.equalsIgnoreCase(job)) {
                return Optional.ofNullable(npcNames.getString(key)).filter(name -> !name.isBlank());
            }
        }
        return Optional.empty();
    }

    private synchronized YamlConfiguration currentYaml() {
        File configFile = CONFIG_FILE_PATH.toFile();
        if (!configFile.exists()) {
            return new YamlConfiguration();
        }
        long lastModified = configFile.lastModified();
        if (lastModified != loadedAt) {
            yamlConfiguration = YamlConfiguration.loadConfiguration(configFile);
            loadedAt = lastModified;
        }
        return yamlConfiguration;
    }
}