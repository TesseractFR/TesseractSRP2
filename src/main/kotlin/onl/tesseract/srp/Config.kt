package onl.tesseract.srp

import onl.tesseract.lib.exception.ConfigurationException
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

data class Config(
    val srpDbHost: String,
    val srpDbPort: Int,
    val srpDbUsername: String,
    val srpDbPassword: String,
    val srpDbDatabase: String
){
    companion object {
        private lateinit var instance: Config

        operator fun invoke(): Config {
            if (this::instance.isInitialized)
                return instance
            instance = load()
            return instance
        }

        fun load(): Config {
            return load(YamlConfiguration.loadConfiguration(File("plugins/Tesseract/config.yml")))
        }

        fun load(yaml: ConfigurationSection): Config {
            return Config(
                srpDbHost = yaml.getString("srp_db_host") ?: throw ConfigurationException("Missing config srp_db_host"),
                srpDbDatabase = yaml.getString("srp_db_database")
                        ?: throw ConfigurationException("Missing config srp_db_database"),
                srpDbUsername = yaml.getString("srp_db_username")
                        ?: throw ConfigurationException("Missing config srp_db_username"),
                srpDbPassword = yaml.getString("srp_db_password")
                        ?: throw ConfigurationException("Missing config srp_db_password"),
                srpDbPort = yaml.getInt("srp_db_port")
            )
        }
    }
}
