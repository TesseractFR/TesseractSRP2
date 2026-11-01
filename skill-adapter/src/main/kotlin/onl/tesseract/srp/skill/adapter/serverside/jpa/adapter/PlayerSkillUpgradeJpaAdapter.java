package onl.tesseract.srp.skill.adapter.serverside.jpa.adapter;

import onl.tesseract.srp.skill.adapter.serverside.jpa.entity.PlayerSkillEmbeddable;
import onl.tesseract.srp.skill.adapter.serverside.jpa.entity.PlayerSkillUpgradeEntity;
import onl.tesseract.srp.skill.adapter.serverside.jpa.repository.PlayerSkillUpgradeJpaRepository;
import onl.tesseract.srp.common.domain.model.PlayerId;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.skill.domain.model.skill.SkillName;
import onl.tesseract.srp.skill.domain.port.serverside.PlayerSkillUpgradeRepository;
import org.springframework.stereotype.Component;

@Component
public class PlayerSkillUpgradeJpaAdapter implements PlayerSkillUpgradeRepository {
    private final PlayerSkillUpgradeJpaRepository repository;

    public PlayerSkillUpgradeJpaAdapter(PlayerSkillUpgradeJpaRepository repository) {
        this.repository = repository;
    }

    private PlayerSkillEmbeddable key(PlayerId player, SkillName skill) {
        return new PlayerSkillEmbeddable(player.value(), skill.value());
    }

    @Override
    public Quality getMaximumIngredientQuality(PlayerId player, SkillName skill) {
        return repository.findById(key(player, skill))
                .map(entity -> Quality.values()[Math.max(0, Math.min(entity.getMaxIngredientQuality(), Quality.values().length - 1))])
                .orElse(Quality.POOR);
    }

    @Override
    public void saveMaximumIngredientQuality(PlayerId player, SkillName skill, Quality quality) {
        PlayerSkillEmbeddable key = key(player, skill);
        repository.save(repository.findById(key)
                .map(entity -> {
                    entity.setMaxIngredientQuality(quality.ordinal());
                    return entity;
                })
                .orElseGet(() -> new PlayerSkillUpgradeEntity(key, quality.ordinal())));
    }
}
