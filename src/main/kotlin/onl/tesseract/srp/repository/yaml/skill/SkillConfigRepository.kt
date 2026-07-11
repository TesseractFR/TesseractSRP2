package onl.tesseract.srp.repository.yaml.skill

import onl.tesseract.lib.exception.ConfigurationException
import onl.tesseract.lib.logger.LoggerFactory
import onl.tesseract.srp.service.item.CustomItemService
import onl.tesseract.srp.skill.domain.model.recipe.*
import onl.tesseract.srp.skill.domain.model.skill.Skill
import onl.tesseract.srp.skill.domain.model.skill.SkillName
import onl.tesseract.srp.skill.domain.model.skill.SkillStructureName
import onl.tesseract.srp.skill.domain.model.skill.SkillTier
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.slf4j.Logger
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import org.springframework.stereotype.Component as SpringComponent

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
        val tiers: Map<Tier, SkillTier> = loadTiers(
            conf.getConfigurationSection("tiers") ?: throw ConfigurationException("The tiers must be set"),
            skillName)
        skills[skillName] = Skill(SkillName(skillName), SkillStructureName(structureName),tiers)

    }

    private fun loadTiers(configurationSection: ConfigurationSection, skillName: String): Map<Tier, SkillTier> {
        val tiers = mutableMapOf<Tier, SkillTier>()
        for (tierKey in configurationSection.getKeys(false)) {
            val tierId = Tier(tierKey.toIntOrNull() ?: throw ConfigurationException("Tier must be an integer for $skillName"))
            val section = configurationSection.getConfigurationSection(tierKey)?: throw ConfigurationException("Tier must be not empty for $skillName.")
            val recipes: Map<RecipeName, Recipe> = loadRecipes(
                section.getConfigurationSection("recipes")
                        ?: throw ConfigurationException("The recipes must be set for tier $tierId for skill $skillName"),
                skillName,
                tierId
            )
            tiers[tierId] = SkillTier(recipes.values.associateBy { it.slot.value() }, recipes.values.associateBy { it.name.value() })
        }
        return tiers
    }

    private fun loadRecipes(
        configurationSection: ConfigurationSection,
        skillName: String,
        tier: Tier
    ): Map<RecipeName, Recipe> {
        val recipes = mutableMapOf<RecipeName, Recipe> ()
        for (recipeKey in configurationSection.getKeys(false)) {
            val name = RecipeName(recipeKey.toString())
            val section = configurationSection.getConfigurationSection(recipeKey)?: throw ConfigurationException("Recipe must be not empty for $skillName")
            val slot = Slot(section.getInt("slot"))
            val result = loadResult(
                section.getConfigurationSection("result")
                        ?: throw ConfigurationException("Recipe must have a result for $skillName"))
            val compos = loadComponents(
                section.getConfigurationSection("components")
                    ?: throw ConfigurationException("Recipe must have components for $skillName"))
            val duration = java.time.Duration.ofSeconds(section.getInt("duration").toLong())
            val recipe = Recipe(name, slot,compos, result, tier,duration)
            recipes[name] = recipe
        }
        return recipes
    }

    private fun loadComponents(configurationSection: ConfigurationSection) : Map<IngredientSlot, RecipeComponent>{
        val components = mutableMapOf<IngredientSlot, RecipeComponent>()
        for (componentKey in configurationSection.getKeys(false)) {
            val compoId = IngredientSlot(componentKey.toIntOrNull() ?: throw ConfigurationException("Component key must be an integer"))
            val compoSection = configurationSection.getConfigurationSection(componentKey)!!
            val quantity = compoSection.getInt("quantity")
            val material = Material(compoSection.getString("material"))
            components[compoId] = RecipeComponent(quantity,material)
        }
        return components
    }

    private fun loadResult(configurationSection: ConfigurationSection): RecipeComponent {
        val quantity = configurationSection.getInt("quantity")
        val material = Material(configurationSection.getString("material"))
        return RecipeComponent(quantity,material)
    }

    fun getSkills(): Map<String, Skill> {
        if (!this::skills.isInitialized || skills.isEmpty()) {
           loadSkills()
        }
        return skills
    }
}