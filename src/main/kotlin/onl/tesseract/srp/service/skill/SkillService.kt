package onl.tesseract.srp.service.skill

import onl.tesseract.srp.domain.skill.crafting.CraftTask
import onl.tesseract.srp.domain.skill.Skill
import onl.tesseract.srp.domain.skill.crafting.QueuedRecipe
import onl.tesseract.srp.domain.skill.recipe.Recipe
import onl.tesseract.srp.domain.item.Quality
import onl.tesseract.srp.domain.port.PlayerInventoryPort
import onl.tesseract.srp.repository.yaml.skill.SkillConfigRepository
import onl.tesseract.srp.service.item.CustomItemService
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

@Component
class SkillService(val skillConfigRepository: SkillConfigRepository,
                   val customItemService: CustomItemService,
                   val playerInventoryPort: PlayerInventoryPort) {
    private val activeTacks = mutableMapOf<UUID, MutableMap<String, CraftTask>>()

    fun getSkillFromStructureID(structureID: String) : Skill?{
        return skillConfigRepository.getSkills()
                .map { it.value }
                .firstOrNull { it.structureName == structureID }

    }

    fun getActiveTask(player: UUID, skillName: String): CraftTask? {
        return activeTacks[player]?.get(skillName)
    }

    fun collectCraftResults(player: UUID, skill: Skill) {
        val task = activeTacks[player]?.get(skill.name) ?: return

        val itemsToGive = mutableListOf<ItemStack>()

        task.done.forEach { itemsToGive.add(customItemService.toItemstack(it)) }
        task.garbage.forEach { itemsToGive.add(customItemService.toItemstack(it)) }

        if (itemsToGive.isEmpty()) return

        playerInventoryPort.giveItems(player, itemsToGive)
        task.done.clear()
        task.garbage.clear()

        Bukkit.getPlayer(player)?.sendMessage("§aVous avez récupéré vos objets !")
    }

    fun startCraft(player: UUID, skill: Skill, recipe: Recipe, quantity: Int) {
        // Vérifier les ressources et les retirer
        for (component in recipe.components.values) {
            val itemStack = customItemService.toItemstack(component.item)
            val totalNeeded = component.quantity * quantity
            val available = playerInventoryPort.getItemNumber(player, itemStack)
            if (available < totalNeeded) {
                Bukkit.getPlayer(player)?.sendMessage("§cVous n'avez pas assez de ressources !")
                return
            }
        }

        for (component in recipe.components.values) {
            val itemStack = customItemService.toItemstack(component.item)
            val totalNeeded = component.quantity * quantity
            playerInventoryPort.removeItems(player, itemStack, totalNeeded)
        }

        val queuedRecipe = QueuedRecipe(recipe, Quality.NORMAL, quantity)
        val craftTask = CraftTask(queuedRecipe, mutableListOf(), mutableListOf(), 5.seconds)

        activeTacks.getOrPut(player) { mutableMapOf() }[skill.name] = craftTask
        Bukkit.getPlayer(player)?.sendMessage("§aFabrication lancée !")
    }

}