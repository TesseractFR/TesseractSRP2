package onl.tesseract.srp.customitem.adapter.serverside;

import onl.tesseract.srp.customitem.domain.model.CustomMaterial;
import onl.tesseract.srp.customitem.domain.model.ItemTag;
import onl.tesseract.srp.customitem.domain.model.MaterialName;
import onl.tesseract.srp.customitem.domain.model.Rarity;
import onl.tesseract.srp.customitem.domain.port.serverside.CustumMaterialRepository;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@Component
public class FileSystemCustumMaterialRepository implements CustumMaterialRepository {


    private final Path path = Path.of("plugins/Tesseract/customitem.yml");

    @Override
    public Set<CustomMaterial> findAll() {
        if(!Files.exists(path)) {
            return Set.of();
        }
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(path.toFile());
        Set<CustomMaterial> customMaterials = new HashSet<>();
        for (String key : yamlConfiguration.getKeys(false)) {
            ConfigurationSection configurationSection = yamlConfiguration.getConfigurationSection(key);
            if(configurationSection == null) {
                continue;
            }
            customMaterials.add(new CustomMaterial(
                    new MaterialName(key.toUpperCase()),
                    new MaterialName(configurationSection.getString("display_name")),
                    new ItemTag(configurationSection.getString("item_tag")),
                    Rarity.valueOf(configurationSection.getString("rarity"))));
        }
        return customMaterials;
    }
}
