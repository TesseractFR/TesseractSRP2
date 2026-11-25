package onl.tesseract.srp.commission.adapter.serverside.yaml;

import onl.tesseract.srp.commission.domain.model.board.CommissionBoardDeliveryRequirement;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardUpgradeStep;
import onl.tesseract.srp.commission.domain.model.board.CommissionBoardUpgradeType;
import onl.tesseract.srp.commission.domain.model.board.UsageTaxRate;
import onl.tesseract.srp.commission.domain.model.commission.CommissionQualityWeights;
import onl.tesseract.srp.commission.domain.model.commission.CommissionType;
import onl.tesseract.srp.commission.domain.model.commission.QuantityRate;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionConfigRepository;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Charge une fois, à la création du bean, la configuration des commissions depuis {@code commissions.yml}
 * dans un {@link CommissionConfig}. Le plugin copie ce fichier au démarrage avant de créer les beans :
 * il est toujours présent. Une modification du fichier demande un redémarrage.
 */
@Component
public class YamlCommissionConfigRepository implements CommissionConfigRepository, CommissionCatalogConfig {

    private static final Path CONFIG_FILE_PATH = Path.of("plugins/Tesseract/commission/commissions.yml");
    private static final String MONEY_BONUS_PATH = "money-bonus";

    private final CommissionConfig commissionConfig;

    public YamlCommissionConfigRepository() {
        this.commissionConfig = load(CONFIG_FILE_PATH);
    }

    @Override
    public double getSpawnRewardRate() {
        return commissionConfig.spawnRewardRate();
    }

    @Override
    public double getMoneyBonusRate(int moneyBonusLevel) {
        return commissionConfig.getMoneyBonusRate(moneyBonusLevel);
    }

    @Override
    public double getGuildXpPerLys() {
        return commissionConfig.guildXpPerLys();
    }

    @Override
    public UsageTaxRate getDefaultUsageTaxRate() {
        return commissionConfig.defaultUsageTaxRate();
    }

    @Override
    public CommissionQualityWeights getQualityWeights() {
        return commissionConfig.qualityWeights();
    }

    @Override
    public QuantityRate getQuantityRate(Quality minimalQuality) {
        return commissionConfig.getQuantityRate(minimalQuality);
    }

    @Override
    public int getSpawnSlotsPerType() {
        return commissionConfig.spawnSlotsPerType();
    }

    @Override
    public Optional<CommissionBoardUpgradeStep> findUpgradeStep(CommissionBoardUpgradeType upgradeType, int upgradeLevel) {
        return commissionConfig.findUpgradeStep(upgradeType, upgradeLevel);
    }

    @Override
    public Map<MaterialName, Integer> findAllHarvestTiers() {
        return commissionConfig.harvestTiers();
    }

    @Override
    public CommissionRarityQuantity getRarityQuantity(String rarityName) {
        return commissionConfig.getRarityQuantity(rarityName);
    }

    @Override
    public double getRarityWeight(String rarityName) {
        return commissionConfig.getRarityWeight(rarityName);
    }

    /** Une valeur invalide empêche le démarrage. */
    private static CommissionConfig load(Path configFilePath) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(configFilePath.toFile());
        Map<MaterialName, Integer> harvestTiers = new LinkedHashMap<>();
        readSection(yaml, "harvest-tiers", ConfigurationSection::getInt)
                .forEach((material, tier) -> harvestTiers.put(new MaterialName(material), tier));
        Map<Integer, Double> moneyBonusRates = new HashMap<>();
        readSection(yaml, "upgrades." + MONEY_BONUS_PATH, (section, level) -> section.getDouble(level + ".value"))
                .forEach((level, rate) -> moneyBonusRates.put(Integer.parseInt(level), rate));
        return new CommissionConfig(
                yaml.getDouble("spawn-reward-rate"),
                yaml.getDouble("guild-xp-per-lys"),
                readDefaultUsageTaxRate(yaml),
                yaml.getInt("spawn-slots-per-type"),
                readSection(yaml, "rarity-weights", ConfigurationSection::getDouble),
                readSection(yaml, "quantity-by-rarity", YamlCommissionConfigRepository::readRarityQuantity),
                readQualityWeights(yaml),
                readByQuality(yaml, "quantity-rate-by-quality", (section, qualityName) -> new QuantityRate(section.getDouble(qualityName))),
                harvestTiers,
                readUpgradeSteps(yaml),
                moneyBonusRates);
    }

    /** Le fichier garde un taux décimal ({@code 0.25} pour 25 %), arrondi ici au pourcentage entier. */
    private static UsageTaxRate readDefaultUsageTaxRate(ConfigurationSection yaml) {
        return new UsageTaxRate((int) Math.round(yaml.getDouble("default-usage-tax-rate") * 100));
    }

    /** Une qualité absente de la section a un poids nul. */
    private static CommissionQualityWeights readQualityWeights(ConfigurationSection yaml) {
        return new CommissionQualityWeights(readByQuality(yaml, "quality-weights", ConfigurationSection::getDouble));
    }

    /** @return les valeurs de la section indexées par qualité ; un nom de qualité inconnu empêche le démarrage */
    private static <T> Map<Quality, T> readByQuality(ConfigurationSection yaml, String sectionName,
                                                     BiFunction<ConfigurationSection, String, T> valueReader) {
        Map<Quality, T> values = new EnumMap<>(Quality.class);
        readSection(yaml, sectionName, valueReader).forEach((qualityName, value) -> values.put(Quality.valueOf(qualityName), value));
        return values;
    }

    /** Lit une rareté sous la forme {@code { quantity: 300, variance: 50 }}. */
    private static CommissionRarityQuantity readRarityQuantity(ConfigurationSection section, String rarityName) {
        return new CommissionRarityQuantity(section.getInt(rarityName + ".quantity"), section.getDouble(rarityName + ".variance"));
    }

    /** Les paliers sont indexés par amélioration puis par niveau à atteindre. */
    private static Map<CommissionBoardUpgradeType, Map<Integer, CommissionBoardUpgradeStep>> readUpgradeSteps(ConfigurationSection yaml) {
        Map<CommissionBoardUpgradeType, Map<Integer, CommissionBoardUpgradeStep>> upgradeSteps =
                new EnumMap<>(CommissionBoardUpgradeType.class);
        for (CommissionBoardUpgradeType upgradeType : CommissionBoardUpgradeType.values()) {
            Map<Integer, CommissionBoardUpgradeStep> steps = new HashMap<>();
            readSection(yaml, "upgrades." + toUpgradePath(upgradeType), YamlCommissionConfigRepository::readUpgradeStep)
                    .forEach((level, step) -> steps.put(Integer.parseInt(level), step));
            upgradeSteps.put(upgradeType, steps);
        }
        return upgradeSteps;
    }

    private static CommissionBoardUpgradeStep readUpgradeStep(ConfigurationSection upgradeSection, String level) {
        ConfigurationSection stepSection = upgradeSection.getConfigurationSection(level);
        if (stepSection == null) {
            throw new IllegalArgumentException("Le palier " + upgradeSection.getCurrentPath() + "." + level + " doit être une section");
        }
        return new CommissionBoardUpgradeStep(stepSection.getInt("cost"), stepSection.getInt("board-tier", 1),
                readDeliveryRequirements(stepSection));
    }

    /** Les conditions de livraison sont indexées par type de commission : {@code HARVEST: { quality: GOOD, count: 100 }}. */
    private static List<CommissionBoardDeliveryRequirement> readDeliveryRequirements(ConfigurationSection stepSection) {
        return List.copyOf(readSection(stepSection, "deliveries", (deliveries, typeName) -> {
            String qualityName = deliveries.getString(typeName + ".quality");
            if (qualityName == null) {
                throw new IllegalArgumentException("Qualité manquante pour la condition " + deliveries.getCurrentPath() + "." + typeName);
            }
            return new CommissionBoardDeliveryRequirement(CommissionType.valueOf(typeName), Quality.valueOf(qualityName),
                    deliveries.getInt(typeName + ".count"));
        }).values());
    }

    private static String toUpgradePath(CommissionBoardUpgradeType upgradeType) {
        return switch (upgradeType) {
            case BOARD_TIER -> "board-tier";
            case MAX_HARVEST_TIER -> "max-harvest-tier";
            case MAX_CRAFT_TIER -> "max-craft-tier";
            case HARVEST_SLOTS -> "harvest-slots";
            case CRAFT_SLOTS -> "craft-slots";
            case MONEY_BONUS -> MONEY_BONUS_PATH;
        };
    }

    /** @return les valeurs de la section indexées par clé, dans l'ordre du fichier, table vide si la section est absente */
    private static <T> Map<String, T> readSection(ConfigurationSection yaml, String sectionName,
                                                  BiFunction<ConfigurationSection, String, T> valueReader) {
        Map<String, T> values = new LinkedHashMap<>();
        Optional.ofNullable(yaml.getConfigurationSection(sectionName))
                .ifPresent(section -> section.getKeys(false)
                        .forEach(key -> values.put(key, valueReader.apply(section, key))));
        return values;
    }
}
