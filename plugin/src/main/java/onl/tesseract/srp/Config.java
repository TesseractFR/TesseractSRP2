package onl.tesseract.srp;

import onl.tesseract.lib.exception.ConfigurationException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public record Config(String srpDbHost, int srpDbPort, String srpDbUsername, String srpDbPassword, String srpDbDatabase,
                     boolean forceUpdateConfig) {
    public Config() {
        this("localhost", 3306, "root", "", "tesseract_srp", false);
    }

    private static volatile Config instance;

    public static synchronized Config getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public static Config load() {
        return load(YamlConfiguration.loadConfiguration(new File("plugins/Tesseract/config.yml")));
    }

    public static Config load(ConfigurationSection yaml) {
        String srpDbHost = yaml.getString("srp_db_host");
        if (srpDbHost == null) throw new ConfigurationException("Missing config srp_db_host");
        String srpDbDatabase = yaml.getString("srp_db_database");
        if (srpDbDatabase == null) throw new ConfigurationException("Missing config srp_db_database");
        String srpDbUsername = yaml.getString("srp_db_username");
        if (srpDbUsername == null) throw new ConfigurationException("Missing config srp_db_username");
        String srpDbPassword = yaml.getString("srp_db_password");
        if (srpDbPassword == null) throw new ConfigurationException("Missing config srp_db_password");
        return new Config(
                srpDbHost,
                yaml.getInt("srp_db_port"),
                srpDbUsername,
                srpDbPassword,
                srpDbDatabase,
                yaml.getBoolean("force_update_config", false)
        );
    }
}

