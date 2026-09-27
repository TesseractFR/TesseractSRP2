package onl.tesseract.srp.skill.adapter.serverside.yaml;

import onl.tesseract.lib.exception.ConfigurationException;
import onl.tesseract.srp.skill.domain.model.recipe.*;
import onl.tesseract.srp.skill.domain.model.skill.Skill;
import onl.tesseract.srp.skill.domain.model.skill.SkillName;
import onl.tesseract.srp.skill.domain.model.skill.SkillStructureName;
import onl.tesseract.srp.skill.domain.model.skill.SkillTier;
import onl.tesseract.srp.skill.domain.port.serverside.SkillConfigRepository;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Component
public class YamlSkillConfigRepository implements SkillConfigRepository {

    private Map<String, Skill> skills;

    private void loadSkills() {
        Path path = Path.of("plugins/Tesseract/artisanat");
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            throw new ConfigurationException("The directory artisanat doesn't exist!");
        }
        skills = new HashMap<>();
        try(var list = Files.list(path)) {
            list.forEach(filePath -> {
                if (Files.isRegularFile(filePath) && filePath.getFileName().toString().endsWith(".yml")) {
                    var conf = YamlConfiguration.loadConfiguration(filePath.toFile());
                    loadSkill(filePath, conf);
                }
            });
        } catch (Exception e) {
            throw new ConfigurationException("Error loading skills: " + e.getMessage(), e);
        }
    }

    private void loadSkill(Path filePath, YamlConfiguration conf) {
        String skillName = conf.getString("name");
        if (skillName == null) {
            throw new ConfigurationException("The name must be set for " + filePath);
        }
        String structureName = conf.getString("structure_name");
        if (structureName == null) {
            throw new ConfigurationException("The structureName must be set for " + skillName);
        }
        ConfigurationSection tiersSection = conf.getConfigurationSection("tiers");
        if (tiersSection == null) {
            throw new ConfigurationException("The tiers must be set for " + skillName);
        }
        Map<Tier, SkillTier> tiers = loadTiers(tiersSection, skillName);
        skills.put(skillName, new Skill(new SkillName(skillName), new SkillStructureName(structureName), tiers));
    }

    private Map<Tier, SkillTier> loadTiers(ConfigurationSection configurationSection, String skillName) {
        Map<Tier, SkillTier> tiers = new HashMap<>();
        for (String tierKey : configurationSection.getKeys(false)) {
            Integer tierIdInt = null;
            try {
                tierIdInt = Integer.parseInt(tierKey);
            } catch (NumberFormatException e) {
                throw new ConfigurationException("Tier must be an integer for " + skillName);
            }
            Tier tierId = new Tier(tierIdInt);
            ConfigurationSection tierSection = configurationSection.getConfigurationSection(tierKey);
            if (tierSection == null) {
                throw new ConfigurationException("Tier must be not empty for " + skillName);
            }
            ConfigurationSection recipesSection = tierSection.getConfigurationSection("recipes");
            if (recipesSection == null) {
                throw new ConfigurationException("The recipes must be set for tier " + tierId.value() + " for skill " + skillName);
            }
            Map<RecipeName, Recipe> recipes = loadRecipes(recipesSection, skillName, tierId);
            Map<Integer, Recipe> slotMap = new HashMap<>();
            Map<String, Recipe> nameMap = new HashMap<>();
            for (Recipe recipe : recipes.values()) {
                slotMap.put(recipe.slot().value(), recipe);
                nameMap.put(recipe.name().value(), recipe);
            }
            tiers.put(tierId, new SkillTier(slotMap, nameMap));
        }
        return tiers;
    }

    private Map<RecipeName, Recipe> loadRecipes(ConfigurationSection configurationSection, String skillName, Tier tier) {
        Map<RecipeName, Recipe> recipes = new HashMap<>();
        for (String recipeKey : configurationSection.getKeys(false)) {
            RecipeName name = new RecipeName(recipeKey);
            ConfigurationSection recipeSection = configurationSection.getConfigurationSection(recipeKey);
            if (recipeSection == null) {
                throw new ConfigurationException("Recipe must be not empty for " + skillName);
            }
            int slotInt = recipeSection.getInt("slot");
            Slot slot = new Slot(slotInt);
            ConfigurationSection resultSection = recipeSection.getConfigurationSection("result");
            if (resultSection == null) {
                throw new ConfigurationException("Recipe must have a result for " + skillName);
            }
            RecipeComponent result = loadResult(resultSection);
            ConfigurationSection componentsSection = recipeSection.getConfigurationSection("components");
            if (componentsSection == null) {
                throw new ConfigurationException("Recipe must have components for " + skillName);
            }
            Map<IngredientSlot, RecipeComponent> components = loadComponents(componentsSection);
            int durationSeconds = recipeSection.getInt("duration");
            java.time.Duration duration = java.time.Duration.ofSeconds(durationSeconds);
            recipes.put(name, new Recipe(name, slot, components, result, tier, duration));
        }
        return recipes;
    }

    private Map<IngredientSlot, RecipeComponent> loadComponents(ConfigurationSection configurationSection) {
        Map<IngredientSlot, RecipeComponent> components = new HashMap<>();
        for (String componentKey : configurationSection.getKeys(false)) {
            Integer compoIdInt = null;
            try {
                compoIdInt = Integer.parseInt(componentKey);
            } catch (NumberFormatException e) {
                throw new ConfigurationException("Component key must be an integer");
            }
            IngredientSlot compoId = new IngredientSlot(compoIdInt);
            ConfigurationSection compoSection = configurationSection.getConfigurationSection(componentKey);
            if (compoSection == null) {
                throw new ConfigurationException("Component section is null for " + componentKey);
            }
            int quantity = compoSection.getInt("quantity");
            String materialStr = compoSection.getString("material");
            Material material = new Material(materialStr);
            components.put(compoId, new RecipeComponent(quantity, material));
        }
        return components;
    }

    private RecipeComponent loadResult(ConfigurationSection configurationSection) {
        int quantity = configurationSection.getInt("quantity");
        String materialStr = configurationSection.getString("material");
        Material material = new Material(materialStr);
        return new RecipeComponent(quantity, material);
    }


    @Override
    public Map<String, Skill> getSkills() {
        if (skills == null || skills.isEmpty()) {
            loadSkills();
        }
        return skills;
    }
}
