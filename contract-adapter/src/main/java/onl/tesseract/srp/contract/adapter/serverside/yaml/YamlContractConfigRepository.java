package onl.tesseract.srp.contract.adapter.serverside.yaml;

import onl.tesseract.srp.contract.domain.model.contract.ContractClient;
import onl.tesseract.srp.contract.domain.port.serverside.ContractConfigRepository;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

/** Lit la configuration des contrats depuis le fichier YAML du serveur et recharge ses changements. */
@Component
public class YamlContractConfigRepository implements ContractConfigRepository, ContractNpcNameConfig,
        ContractRarityConfig, DailyTicketConfig {

    private static final Path CONFIG_FILE_PATH = Path.of("plugins/Tesseract/contract/contracts.yml");

    private YamlConfiguration yamlConfiguration = new YamlConfiguration();
    private long loadedAt = -1;

    @Override
    public Duration getScrollLifetime() {
        return Duration.ofHours(currentYaml().getLong("scroll-lifetime-hours", 72));
    }

    @Override
    public double getQuantityByRarity(String rarityName) {
        return currentYaml().getDouble("quantity-by-rarity." + rarityName, 300);
    }

    @Override
    public double getRarityWeight(String rarityName) {
        return currentYaml().getDouble("rarity-weights." + rarityName, 0);
    }

    @Override
    public int getDailyTickets(String rankName) {
        return currentYaml().getInt("daily-tickets." + rankName, 0);
    }

    @Override
    public Optional<String> findNpcName(ContractClient client) {
        return Optional.ofNullable(currentYaml().getConfigurationSection("npc-names"))
                .flatMap(npcNames -> findIgnoringCase(npcNames, client.name()));
    }

    private static Optional<String> findIgnoringCase(ConfigurationSection section, String key) {
        return section.getKeys(false).stream()
                .filter(candidate -> candidate.equalsIgnoreCase(key))
                .findFirst()
                .flatMap(candidate -> Optional.ofNullable(section.getString(candidate)))
                .filter(name -> !name.isBlank());
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
