package onl.tesseract.srp.skill.adapter.serverside.jpa.adapter;

import jakarta.transaction.Transactional;
import onl.tesseract.srp.skill.adapter.serverside.jpa.entity.PlayerSkillEmbeddable;
import onl.tesseract.srp.skill.adapter.serverside.jpa.entity.craft.CraftBonusEmbeddable;
import onl.tesseract.srp.skill.adapter.serverside.jpa.entity.craft.CraftingEntity;
import onl.tesseract.srp.skill.adapter.serverside.jpa.entity.craft.RecipeEmbeddable;
import onl.tesseract.srp.skill.adapter.serverside.jpa.repository.CraftingJpaRepository;
import onl.tesseract.srp.skill.domain.model.PlayerID;
import onl.tesseract.srp.skill.domain.model.crafting.CraftTask;
import onl.tesseract.srp.skill.domain.model.crafting.QueuedRecipe;
import onl.tesseract.srp.skill.domain.model.skill.SkillName;
import onl.tesseract.srp.skill.domain.port.serverside.CraftingRepository;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CraftingRepositoryJpaAdapter implements CraftingRepository {

    private final CraftingJpaRepository craftingJpaRepository;

    public CraftingRepositoryJpaAdapter(CraftingJpaRepository craftingJpaRepository) {
        this.craftingJpaRepository = craftingJpaRepository;
    }

    @Override
    public void removeActiveTask(PlayerID player, SkillName skillName) {
        craftingJpaRepository.deleteByPlayerSkillAndQueueOrder(
            new PlayerSkillEmbeddable(player.value(), skillName.value()),
            0
        );
    }

    @Override
    public void saveActiveTask(PlayerID player, SkillName skill, CraftTask task) {
        PlayerSkillEmbeddable playerSkill = new PlayerSkillEmbeddable(player.value(), skill.value());
        
        // Delete existing active task for this player/skill (queueOrder = 0)
        craftingJpaRepository.deleteByPlayerSkillAndQueueOrder(playerSkill, 0);
        
        // Create and save the new active task
        CraftingEntity entity = new CraftingEntity();
        entity.setPlayerSkill(playerSkill);
        entity.setQueueOrder(0);
        entity.setRecipe(new RecipeEmbeddable(
            task.queuedRecipe().getRecipe().name().value(),
            task.queuedRecipe().getCompoQuality(),
            task.queuedRecipe().getQuantity()
        ));
        entity.setCraftBonus(new CraftBonusEmbeddable(
            task.queuedRecipe().getBonus().craftingStationBonus().qualityBonus().value(),
            task.queuedRecipe().getBonus().craftingStationBonus().successBonus().value()
        ));
        entity.setUnitDurationNanos(task.unitDuration().toSeconds());
        entity.setTimeLeftNanos(task.timeLeft().toNanos());
        
        craftingJpaRepository.save(entity);
    }

    @Override
    public void saveQueuedRecipe(PlayerID player, SkillName skill, Queue<QueuedRecipe> queuedRecipes) {
        if (queuedRecipes == null || queuedRecipes.isEmpty()) {
            return;
        }
        
        PlayerSkillEmbeddable playerSkill = new PlayerSkillEmbeddable(player.value(), skill.value());
        
        // Delete existing queued tasks for this player/skill (queueOrder > 0)
        craftingJpaRepository.deleteQueueByPlayerSkill(player.value(), skill.value());
        
        // Save each queued recipe with incremental queueOrder
        AtomicInteger queueOrder = new AtomicInteger(1);
        queuedRecipes.forEach(queuedRecipe -> {
            CraftingEntity entity = new CraftingEntity();
            entity.setPlayerSkill(playerSkill);
            entity.setQueueOrder(queueOrder.getAndIncrement());
            entity.setRecipe(new RecipeEmbeddable(
                queuedRecipe.getRecipe().name().value(),
                queuedRecipe.getCompoQuality(),
                queuedRecipe.getQuantity()
            ));
            entity.setCraftBonus(new CraftBonusEmbeddable(
                queuedRecipe.getBonus().craftingStationBonus().qualityBonus().value(),
                queuedRecipe.getBonus().craftingStationBonus().successBonus().value()
            ));
            entity.setUnitDurationNanos(Long.MAX_VALUE);
            entity.setTimeLeftNanos(Long.MAX_VALUE);
            
            craftingJpaRepository.save(entity);
        });
    }
}
