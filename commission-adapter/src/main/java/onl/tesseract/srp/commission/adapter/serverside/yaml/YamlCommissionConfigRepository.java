package onl.tesseract.srp.commission.adapter.serverside.yaml;

import onl.tesseract.srp.commission.domain.model.board.UpgradeStep;
import onl.tesseract.srp.commission.domain.model.board.UpgradeType;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionConfigRepository;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Lit la configuration des commissions dans {@code commissions.yml}. Le fichier est chargé une fois,
 * à la création du bean : une modification demande un redémarrage.
 */
@Component
public class YamlCommissionConfigRepository implements CommissionConfigRepository, CommissionCatalogConfig {

    private static final Path CONFIG_FILE_PATH = Path.of("plugins/Tesseract/commission/commissions.yml");
    private static final double DEFAULT_USAGE_TAX_RATE = 0.25;
    private static final int DEFAULT_SPAWN_SLOTS_PER_TYPE = 6;

    private final ConfigurationSection yaml;

    /** Fichier absent : toutes les valeurs par défaut. */
    public YamlCommissionConfigRepository() {
        File configFile = CONFIG_FILE_PATH.toFile();
        this.yaml = configFile.exists() ? YamlConfiguration.loadConfiguration(configFile) : new YamlConfiguration();
    }

    @Override
    public double getSpawnRewardRate() {
        return yaml.getDouble("spawn-reward-rate", 1);
    }

    @Override
    public double getMoneyBonusRate(int moneyBonusLevel) {
        return yaml.getDouble("upgrades.money-bonus." + moneyBonusLevel + ".value", 0);
    }

    @Override
    public double getGuildXpPerLys() {
        return yaml.getDouble("guild-xp-per-lys", 0);
    }

    @Override
    public double getDefaultUsageTaxRate() {
        return yaml.getDouble("default-usage-tax-rate", DEFAULT_USAGE_TAX_RATE);
    }

    @Override
    public int getSpawnSlotsPerType() {
        return yaml.getInt("spawn-slots-per-type", DEFAULT_SPAWN_SLOTS_PER_TYPE);
    }

    @Override
    public Optional<UpgradeStep> findUpgradeStep(UpgradeType upgradeType, int upgradeLevel) {
        if (upgradeType == UpgradeType.MAX_REQUIRED_QUALITY && upgradeLevel >= Quality.values().length) {
            return Optional.empty();
        }
        return Optional.ofNullable(yaml.getConfigurationSection("upgrades." + toUpgradeStepPath(upgradeType, upgradeLevel)))
                .map(stepSection -> new UpgradeStep(stepSection.getInt("cost"), stepSection.getInt("guild-level")));
    }

    @Override
    public Map<MaterialName, Integer> findAllHarvestTiers() {
        Map<MaterialName, Integer> harvestTiers = new LinkedHashMap<>();
        Optional.ofNullable(yaml.getConfigurationSection("harvest-tiers"))
                .ifPresent(tiersSection -> tiersSection.getKeys(false)
                        .forEach(material -> harvestTiers.put(new MaterialName(material), tiersSection.getInt(material))));
        return harvestTiers;
    }

    @Override
    public RarityQuantity getRarityQuantity(String rarityName) {
        String rarityPath = "quantity-by-rarity." + rarityName;
        return new RarityQuantity(yaml.getInt(rarityPath + ".quantity", 0),
                yaml.getDouble(rarityPath + ".variance", 0));
    }

    @Override
    public double getRarityWeight(String rarityName) {
        return yaml.getDouble("rarity-weights." + rarityName, 0);
    }

    /** Les paliers de qualité sont indexés par le nom de la qualité visée, les autres par leur niveau. */
    private static String toUpgradeStepPath(UpgradeType upgradeType, int upgradeLevel) {
        return switch (upgradeType) {
            case MAX_CRAFT_TIER -> "max-craft-tier." + upgradeLevel;
            case MAX_HARVEST_TIER -> "max-harvest-tier." + upgradeLevel;
            case SLOTS -> "slots." + upgradeLevel;
            case MAX_REQUIRED_QUALITY -> "max-required-quality." + Quality.values()[upgradeLevel].name();
            case MONEY_BONUS -> "money-bonus." + upgradeLevel;
        };
    }
}
