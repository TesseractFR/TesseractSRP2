package onl.tesseract.srp.contract.adapter.serverside.catalog;

import lombok.extern.slf4j.Slf4j;
import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import onl.tesseract.srp.contract.adapter.serverside.skill.SkillCatalogPort;
import onl.tesseract.srp.contract.adapter.serverside.yaml.ContractRarityConfig;
import onl.tesseract.srp.contract.domain.model.contract.CatalogItem;
import onl.tesseract.srp.contract.domain.model.contract.ContractClient;
import onl.tesseract.srp.contract.domain.model.contract.ContractType;
import onl.tesseract.srp.contract.domain.port.serverside.ContractCatalogRepository;
import onl.tesseract.srp.customitem.domain.model.CustomMaterial;
import onl.tesseract.srp.customitem.domain.model.Rarity;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;
import onl.tesseract.srp.job.domain.model.Job;
import onl.tesseract.srp.job.domain.model.Material;
import onl.tesseract.srp.job.domain.port.userside.JobService;
import onl.tesseract.srp.skill.domain.model.recipe.Recipe;
import onl.tesseract.srp.skill.domain.model.skill.Skill;
import onl.tesseract.srp.skill.domain.model.skill.SkillTier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Construit le catalogue des contrats à partir des matériaux récoltés par les métiers et fabriqués
 * par les compétences, pondérés selon leur rareté.
 */
@Slf4j
@Component
public class JobSkillContractCatalogRepository implements ContractCatalogRepository {

    private final JobService jobService;
    private final SkillCatalogPort skillCatalogPort;
    private final CustomItemService customItemService;
    private final ContractRarityConfig contractRarityConfig;

    public JobSkillContractCatalogRepository(JobService jobService, SkillCatalogPort skillCatalogPort,
                                             CustomItemService customItemService, ContractRarityConfig contractRarityConfig) {
        this.jobService = jobService;
        this.skillCatalogPort = skillCatalogPort;
        this.customItemService = customItemService;
        this.contractRarityConfig = contractRarityConfig;
    }

    @Override
    public List<CatalogItem> findAllCatalogItems() {
        List<CatalogItem> catalogItems = new ArrayList<>();
        for (Job job : jobService.listJobs()) {
            ContractClient client = new ContractClient(job.jobName().value());
            for (MaterialName material : findAllHarvestedMaterials(job)) {
                addCatalogItem(catalogItems, ContractType.HARVEST, client, material);
            }
        }
        for (Skill skill : skillCatalogPort.findAllSkills()) {
            ContractClient client = new ContractClient(skill.name().value());
            for (MaterialName material : findAllCraftedMaterials(skill)) {
                addCatalogItem(catalogItems, ContractType.CRAFT, client, material);
            }
        }
        return catalogItems;
    }

    @Override
    public MoneyValue getUnitValue(MaterialName material, Quality quality) {
        return customItemService.getMoneyValue(material, quality);
    }

    /** Un matériau sans valeur monétaire ou de poids nul ne peut pas faire l'objet d'un contrat. */
    private void addCatalogItem(List<CatalogItem> catalogItems, ContractType type, ContractClient client,
                                MaterialName material) {
        if (!customItemService.hasMoneyValue(material)) {
            log.warn("Matériau {} ignoré pour les contrats : aucune valeur monétaire dans itemValue.yml", material);
            return;
        }
        String rarityName = resolveRarity(material).name();
        double weight = contractRarityConfig.getRarityWeight(rarityName);
        if (weight <= 0) {
            return;
        }
        int quantity = (int) Math.max(1, Math.round(contractRarityConfig.getQuantityByRarity(rarityName)));
        catalogItems.add(new CatalogItem(type, client, material, weight, quantity));
    }

    private Rarity resolveRarity(MaterialName material) {
        return Optional.ofNullable(customItemService.getCustomMaterial(material))
                .map(CustomMaterial::rarity)
                .orElse(Rarity.COMMON);
    }

    private static Set<MaterialName> findAllHarvestedMaterials(Job job) {
        Set<MaterialName> distinctMaterials = new LinkedHashSet<>();
        Optional.ofNullable(job.sources())
                .flatMap(sources -> Optional.ofNullable(sources.value()))
                .map(Map::values)
                .ifPresent(outputLists -> outputLists.forEach(outputs -> {
                    for (Material material : outputs) {
                        distinctMaterials.add(new MaterialName(material.value()));
                    }
                }));
        return distinctMaterials;
    }

    private static Set<MaterialName> findAllCraftedMaterials(Skill skill) {
        Set<MaterialName> distinctMaterials = new LinkedHashSet<>();
        for (SkillTier tier : skill.tiers().values()) {
            for (Recipe recipe : tier.recipes().values()) {
                distinctMaterials.add(new MaterialName(recipe.result().material().value()));
            }
        }
        return distinctMaterials;
    }
}
