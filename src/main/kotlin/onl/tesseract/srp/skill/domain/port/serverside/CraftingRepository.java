package onl.tesseract.srp.skill.domain.port.serverside;

import onl.tesseract.srp.skill.domain.model.PlayerID;
import onl.tesseract.srp.skill.domain.model.crafting.CraftTask;
import onl.tesseract.srp.skill.domain.model.crafting.QueuedRecipe;
import onl.tesseract.srp.skill.domain.model.skill.SkillName;

import java.util.Queue;

public interface CraftingRepository {
    /**
     * Retire la derniere task en cours
     * @param player Le joueur
     * @param skillName Le skill
     */
    void removeActiveTask(PlayerID player, SkillName skillName);

    /**
     * Sauvegarde la tâche active pour un joueur/skill
     * @param player Le joueur
     * @param skill Le skill
     * @param task La tâche active
     */
    void saveActiveTask(PlayerID player, SkillName skill, CraftTask task);

    /**
     * Sauvegarde les recettes en queue pour un joueur/skill
     * @param player Le joueur
     * @param skill Le skill
     * @param queuedRecipes Les recettes en attente
     */
    void saveQueuedRecipe(PlayerID player, SkillName skill, Queue<QueuedRecipe> queuedRecipes);
}
