package onl.tesseract.srp.commission.adapter.serverside.catalog;

import lombok.extern.slf4j.Slf4j;
import onl.tesseract.srp.commission.adapter.serverside.yaml.CommissionCatalogConfig;
import onl.tesseract.srp.commission.adapter.serverside.yaml.CommissionRarityQuantity;
import onl.tesseract.srp.commission.domain.model.commission.CommissionCatalogItem;
import onl.tesseract.srp.commission.domain.model.commission.CommissionType;
import onl.tesseract.srp.commission.domain.port.serverside.CommissionCatalogRepository;
import onl.tesseract.srp.common.domain.model.MoneyValue;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.common.domain.model.item.MaterialName;
import onl.tesseract.srp.customitem.domain.model.CustomMaterial;
import onl.tesseract.srp.customitem.domain.model.Rarity;
import onl.tesseract.srp.customitem.domain.port.userside.CustomItemService;
import onl.tesseract.srp.skill.domain.model.recipe.Recipe;
import onl.tesseract.srp.skill.domain.model.skill.Skill;
import onl.tesseract.srp.skill.domain.model.skill.SkillTier;
import onl.tesseract.srp.skill.domain.port.userside.SkillService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Construit le catalogue des commissions : les résultats des recettes des compétences pour
 * l'artisanat, avec le tier de leur recette, et les items listés en configuration pour la récolte.
 * Le poids de tirage et la quantité dépendent de la rareté de l'item.
 */
@Slf4j
@Component
public class SkillCustomItemCommissionCatalogRepository implements CommissionCatalogRepository {

    private final SkillService skillService;
    private final CustomItemService customItemService;
    private final CommissionCatalogConfig commissionCatalogConfig;

    public SkillCustomItemCommissionCatalogRepository(SkillService skillService, CustomItemService customItemService,
                                                      CommissionCatalogConfig commissionCatalogConfig) {
        this.skillService = skillService;
        this.customItemService = customItemService;
        this.commissionCatalogConfig = commissionCatalogConfig;
    }

    @Override
    public List<CommissionCatalogItem> findAllCatalogItems(CommissionType commissionType) {
        List<CommissionCatalogItem> catalogItems = new ArrayList<>();
        findAllItemTiers(commissionType).forEach((material, tier) -> {
            if (customItemService.hasMoneyValue(material)) {
                catalogItems.add(toCatalogItem(material, tier));
            } else {
                log.warn("Matériau {} ignoré pour les commissions : aucune valeur dans itemValue.yml", material.value());
            }
        });
        return catalogItems;
    }

    @Override
    public MoneyValue getUnitValue(MaterialName material, Quality quality) {
        return customItemService.getMoneyValue(material, quality);
    }

    @Override
    public Optional<Integer> findItemTier(CommissionType commissionType, MaterialName material) {
        return Optional.ofNullable(findAllItemTiers(commissionType).get(material));
    }

    private CommissionCatalogItem toCatalogItem(MaterialName material, int tier) {
        String rarityName = resolveRarity(material).name();
        CommissionRarityQuantity rarityQuantity = commissionCatalogConfig.getRarityQuantity(rarityName);
        return new CommissionCatalogItem(material, tier, commissionCatalogConfig.getRarityWeight(rarityName), rarityQuantity.quantity(),
                rarityQuantity.variancePercent());
    }

    /** Un item vanilla n'a pas de rareté : il compte comme commun. */
    private Rarity resolveRarity(MaterialName material) {
        return Optional.ofNullable(customItemService.getCustomMaterial(material))
                .map(CustomMaterial::rarity)
                .orElse(Rarity.COMMON);
    }

    private Map<MaterialName, Integer> findAllItemTiers(CommissionType commissionType) {
        return commissionType == CommissionType.HARVEST ? commissionCatalogConfig.findAllHarvestTiers() : findAllCraftTiers();
    }

    /** Un item fabriqué par plusieurs recettes prend le tier de la plus accessible. */
    private Map<MaterialName, Integer> findAllCraftTiers() {
        Map<MaterialName, Integer> tierByMaterial = new LinkedHashMap<>();
        for (Skill skill : skillService.findAllSkills()) {
            for (SkillTier skillTier : skill.tiers().values()) {
                for (Recipe recipe : skillTier.recipes().values()) {
                    tierByMaterial.merge(new MaterialName(recipe.result().material().value()), recipe.tier().value(), Math::min);
                }
            }
        }
        return tierByMaterial;
    }
}
