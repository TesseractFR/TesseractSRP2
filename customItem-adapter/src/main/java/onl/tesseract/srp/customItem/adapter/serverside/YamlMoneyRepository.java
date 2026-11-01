package onl.tesseract.srp.customItem.adapter.serverside;

import lombok.extern.slf4j.Slf4j;
import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.customitem.domain.model.MaterialName;
import onl.tesseract.srp.customitem.domain.port.serverside.MoneyRepository;
import onl.tesseract.srp.customItem.adapter.serverside.exception.MissingMoneyConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class YamlMoneyRepository implements MoneyRepository {
    private static final Path PATH = Path.of("plugins/Tesseract/itemValue.yml");

    private final HashMap<MaterialName, MoneyValue> values = new HashMap<>();
    private final HashMap<Quality, MoneyValue> qualityRates = new HashMap<>();

    public YamlMoneyRepository() {
        loadValues();
    }

    private void loadValues() {
        if (!Files.exists(PATH)) {
            return;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(PATH.toFile());
        loadValues(yaml, "Vanilla");
        loadValues(yaml, "Custom");
        loadCompositions(yaml);
        loadQualityRates(yaml);
    }

    private void loadQualityRates(YamlConfiguration yaml) {
        var section = yaml.getConfigurationSection("QualityRate");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            Quality quality = Quality.valueOf(key.toUpperCase());
            String rawValue = section.getString(key);
            if (rawValue == null || rawValue.isBlank()) {
                throw new IllegalArgumentException("Missing quality rate for " + key);
            }
            qualityRates.put(quality, new MoneyValue(new BigDecimal(rawValue)));
        }
    }

    private void loadValues(YamlConfiguration yaml, String sectionName) {
        var section = yaml.getConfigurationSection(sectionName);
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            MaterialName materialName = new MaterialName(key);
            if (values.containsKey(materialName)) {
                throw new IllegalArgumentException("Duplicate material name: " + materialName);
            }
            String rawValue = section.getString(key);
            if (rawValue == null || rawValue.isBlank()) {
                throw new IllegalArgumentException("Missing money value for " + sectionName + " item " + key);
            }
            values.putIfAbsent(materialName, new MoneyValue(new BigDecimal(rawValue)));
        }
    }

    private void loadCompositions(YamlConfiguration yaml) {
        var section = yaml.getConfigurationSection("CustomComposed");
        if (section == null) return;
        HashMap<String, Map<MaterialName, Integer>> toResolve = new HashMap<>();
        for (String key : section.getKeys(false)) {
            var compositionSection = section.getConfigurationSection(key);
            if (compositionSection == null || compositionSection.getKeys(false).isEmpty()) {
                throw new IllegalArgumentException("Missing components for CustomComposed item " + key);
            }
            Map<MaterialName, Integer> components = new HashMap<>();
            for (String component : compositionSection.getKeys(false)) {
                int count = compositionSection.getInt(component, -1);
                if (count <= 0) {
                    throw new IllegalArgumentException("Invalid component count for " + key + ": " + component);
                }
                components.put(new MaterialName(component), count);
            }
            toResolve.put(key, Map.copyOf(components));
            resolveCompositions(toResolve);
        }
    }

    private void resolveCompositions(HashMap<String, Map<MaterialName, Integer>> toResolve) {
        boolean hasResolved = false;
        for(String key : toResolve.keySet()) {
            Map<MaterialName, Integer> compositions = toResolve.get(key);
            if(!compositions.keySet().stream().allMatch(values::containsKey)){
                continue;
            }
            BigDecimal total = compositions.entrySet()
                    .stream()
                    .map(entry-> values.get(entry.getKey()).value()
                            .multiply(BigDecimal.valueOf(entry.getValue())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            values.put(new MaterialName(key), new MoneyValue(total));
            toResolve.remove(key);
            hasResolved = true;
        }
        if(toResolve.isEmpty())return;
        if(!hasResolved){
            throw new IllegalStateException("Cannot resolve compositions for " + toResolve.keySet());
        }
        resolveCompositions(toResolve);

    }

    @Override
    public MoneyValue getValue(MaterialName materialName, Quality quality) {
        MoneyValue value = values.get(materialName);
        if (value == null) {
            throw new MissingMoneyConfigurationException(
                    "Aucune valeur monétaire n’est définie dans itemValue.yml pour le matériau "
                            + materialName.value() + ".");
        }
        MoneyValue rate = qualityRates.get(quality);
        if (rate == null) {
            throw new MissingMoneyConfigurationException(
                    "Aucun taux de qualité n’est défini dans itemValue.yml pour la qualité "
                            + quality + ".");
        }
        return value.multiply(rate);
    }

    @Override
    public boolean hasValue(MaterialName materialName) {
        return values.containsKey(materialName);
    }
}
