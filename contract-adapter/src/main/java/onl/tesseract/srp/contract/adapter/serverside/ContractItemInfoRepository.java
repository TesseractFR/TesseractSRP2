package onl.tesseract.srp.contract.adapter.serverside;

import onl.tesseract.srp.contract.domain.model.Quality;
import onl.tesseract.srp.contract.domain.model.contract.ContractType;
import onl.tesseract.srp.contract.domain.port.serverside.ItemInfoRepository;
import onl.tesseract.srp.contract.domain.model.contract.CatalogItem;
import onl.tesseract.srp.contract.adapter.serverside.yaml.YamlContractConfigRepository;
import onl.tesseract.srp.customitem.domain.model.CustomMaterial;
import onl.tesseract.srp.customitem.domain.model.MaterialName;
import onl.tesseract.srp.customitem.domain.model.Rarity;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;
import onl.tesseract.srp.job.domain.model.Job;
import onl.tesseract.srp.job.domain.model.Material;
import onl.tesseract.srp.job.domain.port.userside.JobService;
import onl.tesseract.srp.skill.domain.model.recipe.Recipe;
import onl.tesseract.srp.skill.domain.model.skill.Skill;
import onl.tesseract.srp.skill.domain.model.skill.SkillTier;
import onl.tesseract.srp.skill.domain.port.serverside.SkillConfigRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class ContractItemInfoRepository implements ItemInfoRepository {

    private final JobService jobService;
    private final SkillConfigRepository skills;
    private final CustomItemService customItems;
    private final YamlContractConfigRepository config;

    public ContractItemInfoRepository(JobService jobService, SkillConfigRepository skills,
                                      CustomItemService customItems, YamlContractConfigRepository config) {
        this.jobService = jobService;
        this.skills = skills;
        this.customItems = customItems;
        this.config = config;
    }

    @Override
    public List<CatalogItem> getCatalogItems() {
        List<CatalogItem> items = new ArrayList<>();
        for (Job job : jobService.listJobs()) {
            for (String material : producedMaterials(job)) {
                add(items, ContractType.HARVEST, job.jobName().value(), material);
            }
        }
        for (Skill skill : skills.getSkills().values()) {
            for (String material : craftedMaterials(skill)) {
                add(items, ContractType.CRAFT, skill.name().value(), material);
            }
        }
        return items;
    }

    @Override
    public double getValue(String material, Quality quality) {
        // TODO: remplacer par l'appel au service d'items (getMonetaryValue(matériau, qualité)) quand il existera
        return 1.0;
    }

    private void add(List<CatalogItem> items, ContractType type, String job, String material) {
        if (getValue(material, Quality.POOR) <= 0) {
            return;
        }
        Rarity rarity = rarityOf(material);
        double weight = config.getRarityWeight(rarity.name());
        if (weight <= 0) {
            return;
        }
        int quantity = (int) Math.max(1, Math.round(config.getQuantityByRarity(rarity.name())));
        items.add(new CatalogItem(type, job, material, weight, quantity));
    }

    private Rarity rarityOf(String material) {
        CustomMaterial custom = customItems.getCustomMaterial(new MaterialName(material));
        return custom == null ? Rarity.COMMON : custom.rarity();
    }

    private static Set<String> producedMaterials(Job job) {
        Set<String> distinct = new LinkedHashSet<>();
        if (job.sources() == null || job.sources().value() == null) {
            return distinct;
        }
        for (List<Material> outputs : job.sources().value().values()) {
            for (Material material : outputs) {
                distinct.add(material.value());
            }
        }
        return distinct;
    }

    private static Set<String> craftedMaterials(Skill skill) {
        Set<String> distinct = new LinkedHashSet<>();
        for (SkillTier tier : skill.tiers().values()) {
            for (Recipe recipe : tier.recipes().values()) {
                distinct.add(recipe.result().material().value());
            }
        }
        return distinct;
    }
}