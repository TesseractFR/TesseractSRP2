package onl.tesseract.srp.repository.yaml.artisanat

import onl.tesseract.lib.exception.ConfigurationException
import onl.tesseract.lib.logger.LoggerFactory
import onl.tesseract.srp.domain.craftingjob.RecipeComponent
import onl.tesseract.srp.domain.craftingjob.Recipe
import onl.tesseract.srp.domain.craftingjob.Skill
import onl.tesseract.srp.domain.craftingjob.SkillTier
import onl.tesseract.srp.service.item.CustomItemService
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import org.slf4j.Logger
import org.springframework.stereotype.Component as SpringComponent
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path

private val logger: Logger = LoggerFactory.getLogger(SkillConfigRepository::class.java)

@SpringComponent
class SkillConfigRepository(
    val customItemService: CustomItemService,
) {
    private lateinit var skills: MutableMap<String, Skill>

    private fun loadSkills() {
        val path = Path("plugins/Tesseract/artisanat")
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            throw ConfigurationException("The directory artisanat doesn't exist!")
        }
        skills = mutableMapOf()
        Files.list(path)
                .use { stream ->
                    stream
                            .filter { Files.isRegularFile(it) }
                            .filter {
                                it.fileName.toString()
                                        .endsWith(".yml")
                            }
                            .forEach { filePath ->
                                val conf = YamlConfiguration.loadConfiguration(filePath.toFile())
                                loadSkill(filePath, conf)
                            }

                }
    }

    private fun loadSkill(filePath: Path, conf: YamlConfiguration) {
        val skillName = conf.getString("name") ?: throw ConfigurationException("The name must be set for $filePath")
        val structureName = conf.getString("structure_name")
                ?: throw ConfigurationException("The structureName must be set for $skillName")
        val tiers: Map<Int, SkillTier> = loadTiers(
            conf.getConfigurationSection("tiers") ?: throw ConfigurationException("The tiers must be set"),
            skillName)
        skills[skillName] = Skill(tiers, structureName)

    }

    private fun loadTiers(configurationSection: ConfigurationSection, skillName: String): Map<Int, SkillTier> {
        val tiers = mutableMapOf<Int, SkillTier>()
        for (tierKey in configurationSection.getKeys(false)) {
            val tierId = tierKey.toIntOrNull() ?: throw ConfigurationException("Tier must be an integer for $skillName")
            val section = configurationSection.getConfigurationSection(tierKey)?: throw ConfigurationException("Tier must be not empty for $skillName.")
            val recipes: Map<Int, Recipe> = loadRecipes(
                section.getConfigurationSection("recipes")
                        ?: throw ConfigurationException("The recipes must be set for tier $tierId for skill $skillName"),
                skillName
            )
            tiers[tierId] = SkillTier(recipes)
        }
        return tiers
    }

    private fun loadRecipes(
        configurationSection: ConfigurationSection,
        skillName: String,
    ): Map<Int, Recipe> {
        val recipes = mutableMapOf<Int, Recipe> ()
        for (recipeKey in configurationSection.getKeys(false)) {
            val recipeID = recipeKey.toIntOrNull() ?: throw ConfigurationException("Recipe must be an integer for $skillName")
            val section = configurationSection.getConfigurationSection(recipeKey)?: throw ConfigurationException("Recipe must be not empty for $skillName")
            val result = loadResult(
                section.getConfigurationSection("result")
                        ?: throw ConfigurationException("Recipe must have a result for $skillName"))
            val compos = loadComponents(
                section.getConfigurationSection("components")
                    ?: throw ConfigurationException("Recipe must have components for $skillName"))
            recipes[recipeID] = Recipe(compos,result)
        }
        return recipes
    }

    private fun loadComponents(configurationSection: ConfigurationSection) : Map<Int, RecipeComponent>{
        val components = mutableMapOf<Int, RecipeComponent>()
        for (componentKey in configurationSection.getKeys(false)) {
            val compoId = componentKey.toIntOrNull() ?: throw ConfigurationException("Component key must be an integer")
            val compoSection = configurationSection.getConfigurationSection(componentKey)!!
            val quantity = compoSection.getInt("quantity")
            components[compoId] = RecipeComponent(loadItem(compoSection),quantity)
        }
        return components
    }

    private fun loadResult(configurationSection: ConfigurationSection): RecipeComponent {
        val quantity = configurationSection.getInt("quantity")
        return RecipeComponent(loadItem(configurationSection), quantity)
    }

    private fun loadItem(configurationSection: ConfigurationSection): ItemStack {
        val type =
            configurationSection.getString("type") ?: throw ConfigurationException("A recipe item must have a type.")
        if (type == "vanilla") {
            val mat = configurationSection.getString("material")
                    ?: throw ConfigurationException("A recipe vanilla item must have a material.")
            return ItemStack(Material.valueOf(mat))
        }
        if (type == "custom") {
            val namespaceID = configurationSection.getString("namespaceID")
                    ?: throw ConfigurationException("A recipe custom item must have a namespaceID.")
            return customItemService.getCustomItem(namespaceID)
        }
        throw ConfigurationException("Invalid recipe item type.")
    }

    fun getSkills(): Map<String, Skill> {
        if (!this::skills.isInitialized || skills.isEmpty()) {
           loadSkills()
        }
        return skills
    }
}