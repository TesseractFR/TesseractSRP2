package onl.tesseract.srp.skill.adapter.serverside.jpa.adapter;

import onl.tesseract.srp.skill.adapter.serverside.jpa.entity.ItemEmbeddable;
import onl.tesseract.srp.skill.adapter.serverside.jpa.entity.PlayerSkillEmbeddable;
import onl.tesseract.srp.skill.adapter.serverside.jpa.repository.SkillResultCacheJpaRepository;
import onl.tesseract.srp.skill.domain.model.PlayerID;
import onl.tesseract.srp.skill.domain.model.crafting.CraftElement;
import onl.tesseract.srp.skill.domain.model.crafting.LootCache;
import onl.tesseract.srp.skill.domain.model.crafting.PlayerLootCache;
import onl.tesseract.srp.skill.domain.model.recipe.Material;
import onl.tesseract.srp.skill.domain.model.skill.SkillName;
import onl.tesseract.srp.skill.domain.port.serverside.SkillResultCacheRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SkillResultCacheRepositoryJpaAdapter implements SkillResultCacheRepository {

    private final SkillResultCacheJpaRepository skillResultCacheJpaRepository;

    public SkillResultCacheRepositoryJpaAdapter(SkillResultCacheJpaRepository skillResultCacheJpaRepository) {
        this.skillResultCacheJpaRepository = skillResultCacheJpaRepository;
    }

    @Override
    public Map<PlayerID, PlayerLootCache> findAll() {
        Map<PlayerID, PlayerLootCache> result = new HashMap<>();
        skillResultCacheJpaRepository.findAll().forEach(entity -> {
            PlayerID playerID = new PlayerID(entity.getPlayerSkill().getPlayerUuid());
            result.putIfAbsent(playerID, new PlayerLootCache(new HashMap<>()));

            result.get(playerID).lootCaches().put(
                    new SkillName(entity.getPlayerSkill().getSkillName()),
                    new LootCache(entity.getGarbage().stream().map(this::mapCraftElement).toList(),
                            entity.getDone().stream().map(this::mapCraftElement).toList()));
        });
        return result;
    }

    @Override
    public void delete(@NotNull PlayerID playerID, @NotNull SkillName skillName) {

        skillResultCacheJpaRepository.deleteByPlayerSkill(mapPlayerSkill(playerID, skillName));
    }

    @Override
    public void save(PlayerID player, SkillName skill, LootCache lootCache) {

    }

    private PlayerSkillEmbeddable mapPlayerSkill(PlayerID playerID, SkillName skillName) {
        PlayerSkillEmbeddable playerSkill = new PlayerSkillEmbeddable(playerID.value(), skillName.value());
        return playerSkill;
    }

    private CraftElement mapCraftElement(ItemEmbeddable itemEmbeddable) {
        return new CraftElement(new Material(itemEmbeddable.getMaterial()),
                itemEmbeddable.getQuantity(),
                itemEmbeddable.getQuality());
    }
}
