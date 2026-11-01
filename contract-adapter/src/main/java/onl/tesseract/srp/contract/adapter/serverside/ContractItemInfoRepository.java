package onl.tesseract.srp.contract.adapter.serverside;

import lombok.extern.slf4j.Slf4j;
import onl.tesseract.srp.contract.adapter.utils.QualityConverter;
import onl.tesseract.srp.common.domain.model.enums.Quality;
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

@Slf4j
@Component
public class ContractItemInfoRepository implements ItemInfoRepository {

    private final JobService jobService;
    private final SkillConfigRepository skillConfigRepository;
    private final CustomItemService customItemService;
    private final YamlContractConfigRepository yamlContractConfigRepository;

    public ContractItemInfoRepository(JobService jobService, SkillConfigRepository skillConfigRepository,
                                      CustomItemService customItemService, YamlContractConfigRepository yamlContractConfigRepository) {
        this.jobService = jobService;
        this.skillConfigRepository = skillConfigRepository;
        this.customItemService = customItemService;
        this.yamlContractConfigRepository = yamlContractConfigRepository;
    }

    @Override
    public List<CatalogItem> getCatalogItems() {
        List<CatalogItem> catalogItems = new ArrayList<>();
        for (Job job : jobService.listJobs()) {
            for (String material : producedMaterials(job)) {
                add(catalogItems, ContractType.HARVEST, job.jobName().value(), material);
            }
        }
        for (Skill skill : skillConfigRepository.getSkills().values()) {
            for (String material : craftedMaterials(skill)) {
                add(catalogItems, ContractType.CRAFT, skill.name().value(), material);
            }
        }
        return catalogItems;
    }

    @Override
    public double getValue(String material, Quality quality) {
        return customItemService.getMoneyValue(new MaterialName(material), QualityConverter.toCommon(quality)).value().doubleValue();
    }

    private void add(List<CatalogItem> catalogItems, ContractType type, String job, String material) {
        if (!customItemService.hasMoneyValue(new MaterialName(material))) {
            log.warn("Material {} ignored for contracts: no money value in itemValue.yml", material);
            return;
        }
        Rarity rarity = rarityOf(material);
        double weight = yamlContractConfigRepository.getRarityWeight(rarity.name());
        if (weight <= 0) {
            return;
        }
        int quantity = (int) Math.max(1, Math.round(yamlContractConfigRepository.getQuantityByRarity(rarity.name())));
        catalogItems.add(new CatalogItem(type, job, material, weight, quantity));
    }

    private Rarity rarityOf(String material) {
        CustomMaterial customMaterial = customItemService.getCustomMaterial(new MaterialName(material));
        return customMaterial == null ? Rarity.COMMON : customMaterial.rarity();
    }

    private static Set<String> producedMaterials(Job job) {
        Set<String> distinctMaterials = new LinkedHashSet<>();
        if (job.sources() == null || job.sources().value() == null) {
            return distinctMaterials;
        }
        for (List<Material> outputs : job.sources().value().values()) {
            for (Material material : outputs) {
                distinctMaterials.add(material.value());
            }
        }
        return distinctMaterials;
    }

    private static Set<String> craftedMaterials(Skill skill) {
        Set<String> distinctMaterials = new LinkedHashSet<>();
        for (SkillTier tier : skill.tiers().values()) {
            for (Recipe recipe : tier.recipes().values()) {
                distinctMaterials.add(recipe.result().material().value());
            }
        }
        return distinctMaterials;
    }
}