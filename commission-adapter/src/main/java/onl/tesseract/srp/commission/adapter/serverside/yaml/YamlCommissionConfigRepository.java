package onl.tesseract.srp.commission.adapter.serverside.yaml;

import onl.tesseract.srp.commission.domain.model.board.CommissionBoardDeliveryRequirement;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardUpgradeStep;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardUpgradeType;
import onl.tesseract.srp.commission.domain.model.commission.CommissionType;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionConfigRepository;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
    public Optional<CommissionBoardUpgradeStep> findUpgradeStep(CommissionBoardUpgradeType upgradeType, int upgradeLevel) {
        return Optional.ofNullable(yaml.getConfigurationSection("upgrades." + toUpgradeStepPath(upgradeType) + "." + upgradeLevel))
                .map(stepSection -> new CommissionBoardUpgradeStep(stepSection.getInt("cost"), stepSection.getInt("board-tier", 1),
                        readDeliveryRequirements(stepSection)));
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
    public CommissionRarityQuantity getRarityQuantity(String rarityName) {
        String rarityPath = "quantity-by-rarity." + rarityName;
        return new CommissionRarityQuantity(yaml.getInt(rarityPath + ".quantity", 0),
                yaml.getDouble(rarityPath + ".variance", 0));
    }

    @Override
    public double getRarityWeight(String rarityName) {
        return yaml.getDouble("rarity-weights." + rarityName, 0);
    }

    private static String toUpgradeStepPath(CommissionBoardUpgradeType upgradeType) {
        return switch (upgradeType) {
            case BOARD_TIER -> "board-tier";
            case MAX_HARVEST_TIER -> "max-harvest-tier";
            case MAX_CRAFT_TIER -> "max-craft-tier";
            case HARVEST_SLOTS -> "harvest-slots";
            case CRAFT_SLOTS -> "craft-slots";
            case MONEY_BONUS -> "money-bonus";
        };
    }

    /** Les conditions de livraison sont indexées par type de commission : {@code HARVEST: { quality: GOOD, count: 100 }}. */
    private static List<CommissionBoardDeliveryRequirement> readDeliveryRequirements(ConfigurationSection stepSection) {
        List<CommissionBoardDeliveryRequirement> requirements = new ArrayList<>();
        Optional.ofNullable(stepSection.getConfigurationSection("deliveries")).ifPresent(deliveries -> {
            for (String typeName : deliveries.getKeys(false)) {
                ConfigurationSection requirement = deliveries.getConfigurationSection(typeName);
                requirements.add(new CommissionBoardDeliveryRequirement(CommissionType.valueOf(typeName),
                        Quality.valueOf(requirement.getString("quality")), requirement.getInt("count")));
            }
        });
        return requirements;
    }
}
