package onl.tesseract.srp.contract.adapter.serverside.yaml;

import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.contract.domain.model.contract.ContractClient;
import onl.tesseract.srp.contract.domain.model.contract.ContractQualityWeights;
import onl.tesseract.srp.contract.domain.model.contract.QuantityRate;
import onl.tesseract.srp.contract.domain.port.serverside.ContractConfigRepository;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Charge une fois, à la création du bean, la configuration des contrats depuis le fichier YAML du
 * serveur dans un {@link ContractConfig}. Une modification du fichier demande un redémarrage.
 */
@Component
public class YamlContractConfigRepository implements ContractConfigRepository, ContractNpcNameConfig,
        ContractRarityConfig, DailyTicketConfig {

    private static final Path CONFIG_FILE_PATH = Path.of("plugins/Tesseract/contract/contracts.yml");

    private final ContractConfig contractConfig;

    public YamlContractConfigRepository() {
        this.contractConfig = load(CONFIG_FILE_PATH);
    }

    @Override
    public Duration getScrollLifetime() {
        return contractConfig.scrollLifetime();
    }

    @Override
    public ContractQualityWeights getQualityWeights() {
        return contractConfig.qualityWeights();
    }

    @Override
    public QuantityRate getQuantityRate(Quality minimalQuality) {
        return new QuantityRate(contractConfig.getQuantityRate(minimalQuality.name()));
    }

    @Override
    public RarityQuantity getRarityQuantity(String rarityName) {
        return contractConfig.getRarityQuantity(rarityName);
    }

    @Override
    public double getRarityWeight(String rarityName) {
        return contractConfig.getRarityWeight(rarityName);
    }

    @Override
    public int getDailyTickets(String rankName) {
        return contractConfig.getDailyTickets(rankName);
    }

    @Override
    public Optional<String> findNpcName(ContractClient client) {
        return contractConfig.findNpcName(client.name());
    }

    /** Fichier absent : configuration par défaut. Une valeur invalide empêche le démarrage. */
    private static ContractConfig load(Path configFilePath) {
        if (!Files.exists(configFilePath)) {
            return ContractConfig.DEFAULT;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(configFilePath.toFile());
        Map<String, String> npcNames = new HashMap<>();
        readSection(yaml, "npc-names", ConfigurationSection::getString).forEach((client, npcName) -> {
            if (npcName != null && !npcName.isBlank()) {
                npcNames.put(client.toLowerCase(Locale.ROOT), npcName);
            }
        });
        return new ContractConfig(
                Duration.ofHours(yaml.getLong("scroll-lifetime-hours", ContractConfig.DEFAULT_SCROLL_LIFETIME.toHours())),
                readSection(yaml, "quantity-by-rarity", YamlContractConfigRepository::readRarityQuantity),
                readSection(yaml, "quantity-rate-by-quality", ConfigurationSection::getDouble),
                readSection(yaml, "rarity-weights", ConfigurationSection::getDouble),
                readQualityWeights(yaml),
                readSection(yaml, "daily-tickets", ConfigurationSection::getInt),
                npcNames);
    }

    /**
     * Lit la section {@code quality-weights}, indexée par nom de qualité. Section absente : poids par défaut.
     * Une qualité absente de la section a un poids nul ; un nom de qualité inconnu empêche le démarrage.
     */
    private static ContractQualityWeights readQualityWeights(ConfigurationSection yaml) {
        Map<String, Double> weightsByName = readSection(yaml, "quality-weights", ConfigurationSection::getDouble);
        if (weightsByName.isEmpty()) {
            return ContractQualityWeights.DEFAULT;
        }
        Map<Quality, Double> weights = new EnumMap<>(Quality.class);
        weightsByName.forEach((qualityName, weight) ->
                weights.put(Quality.valueOf(qualityName.toUpperCase(Locale.ROOT)), weight));
        return new ContractQualityWeights(weights);
    }

    /**
     * Lit une rareté sous la forme {@code { quantity: 600, variance: 50 }}, ou sous l'ancienne forme
     * d'un simple nombre, qui donne une variance nulle.
     */
    private static RarityQuantity readRarityQuantity(ConfigurationSection section, String rarityName) {
        double defaultQuantity = RarityQuantity.DEFAULT.quantity();
        return Optional.ofNullable(section.getConfigurationSection(rarityName))
                .map(rarity -> new RarityQuantity(rarity.getDouble("quantity", defaultQuantity),
                        rarity.getDouble("variance", 0)))
                .orElseGet(() -> new RarityQuantity(section.getDouble(rarityName, defaultQuantity), 0));
    }

    /** @return les valeurs de la section indexées par clé, table vide si la section est absente */
    private static <T> Map<String, T> readSection(ConfigurationSection yaml, String sectionName,
                                                  BiFunction<ConfigurationSection, String, T> valueReader) {
        Map<String, T> values = new HashMap<>();
        Optional.ofNullable(yaml.getConfigurationSection(sectionName))
                .ifPresent(section -> section.getKeys(false)
                        .forEach(key -> values.put(key, valueReader.apply(section, key))));
        return values;
    }
}
