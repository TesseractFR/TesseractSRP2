package onl.tesseract.srp.service.skill

import onl.tesseract.srp.PLUGIN_INSTANCE
import onl.tesseract.srp.domain.item.CustomItem
import onl.tesseract.srp.domain.skill.crafting.CraftTask
import onl.tesseract.srp.domain.skill.Skill
import onl.tesseract.srp.domain.skill.crafting.QueuedRecipe
import onl.tesseract.srp.domain.skill.recipe.CustomComponentWrapper
import onl.tesseract.srp.domain.skill.recipe.Recipe
import onl.tesseract.srp.domain.item.Quality
import onl.tesseract.srp.domain.port.PlayerInventoryPort
import onl.tesseract.srp.infrastructure.scheduler.skill.CraftTaskScheduler
import onl.tesseract.srp.repository.yaml.skill.SkillConfigRepository
import onl.tesseract.srp.service.item.CustomItemService
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.let
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@Component
class SkillService(val skillConfigRepository: SkillConfigRepository,
                   val customItemService: CustomItemService,
                   val playerInventoryPort: PlayerInventoryPort,
                   val craftTaskScheduler: CraftTaskScheduler) {
    private val activeTasks = mutableMapOf<UUID, MutableMap<String, CraftTask>>()
    private val queues = mutableMapOf<UUID, MutableMap<String, MutableList<QueuedRecipe>>>()
    private val collectionCache = mutableMapOf<UUID, MutableMap<String, CollectionCache>>()

    private data class CollectionCache(
        val done: MutableList<CustomItem> = mutableListOf(),
        val garbage: MutableList<CustomItem> = mutableListOf()
    )

    fun getSkillFromStructureID(structureID: String) : Skill?{
        return skillConfigRepository.getSkills()
                .map { it.value }
                .firstOrNull { it.structureName == structureID }

    }

    fun getActiveTask(player: UUID, skillName: String): CraftTask? {
        return activeTasks[player]?.get(skillName)
    }

    fun getQueue(player: UUID, skillName: String): List<QueuedRecipe> {
        return queues[player]?.get(skillName) ?: emptyList()
    }

    fun collectCraftResults(player: UUID, skill: Skill) {
        val cache = collectionCache[player]?.get(skill.name) ?: return

        val itemsToGive = mutableListOf<ItemStack>()

        cache.done.forEach { itemsToGive.add(customItemService.toItemstack(it)) }
        cache.garbage.forEach { itemsToGive.add(customItemService.toItemstack(it)) }

        if (itemsToGive.isEmpty()) return

        playerInventoryPort.giveItems(player, itemsToGive)
        cache.done.clear()
        cache.garbage.clear()

        Bukkit.getPlayer(player)?.sendMessage("§aVous avez récupéré vos objets !")
    }

    fun hasItemsToCollect(player: UUID, skillName: String): Boolean {
        val cache = collectionCache[player]?.get(skillName) ?: return false
        return cache.done.isNotEmpty() || cache.garbage.isNotEmpty()
    }

    fun getItemsToCollectCount(player: UUID, skillName: String): Pair<Int, Int> {
        val cache = collectionCache[player]?.get(skillName) ?: return 0 to 0
        return cache.done.size to cache.garbage.size
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
        
        val skillQueues = queues.getOrPut(player) { mutableMapOf() }
        val queue = skillQueues.getOrPut(skill.name) { mutableListOf() }
        
        if (activeTasks[player]?.get(skill.name) == null) {
            val craftTask = CraftTask(queuedRecipe, mutableListOf(), mutableListOf(), 5.seconds)
            activeTasks.getOrPut(player) { mutableMapOf() }[skill.name] = craftTask
            craftTaskScheduler.schedule(player, skill, craftTask, this)
            Bukkit.getPlayer(player)?.sendMessage("§aFabrication lancée !")
        } else {
            queue.add(queuedRecipe)
            Bukkit.getPlayer(player)?.sendMessage("§aRecette ajoutée à la file d'attente !")
        }
    }

    private fun mergeIntoCache(targetList: MutableList<CustomItem>, sourceList: List<CustomItem>) {
        for (sourceItem in sourceList) {
            val existingItemIndex = targetList.indexOfFirst { it.material == sourceItem.material && it.quality == sourceItem.quality }
            if (existingItemIndex != -1) {
                val existingItem = targetList[existingItemIndex]
                targetList[existingItemIndex] = existingItem.copy(quantity = existingItem.quantity + sourceItem.quantity)
            } else {
                targetList.add(sourceItem.copy())
            }
        }
    }

    fun processCraftStep(player: UUID, skill: Skill, task: CraftTask) {
        if (task.queuedRecipe.quantity <= 0) {
            finishTask(player, skill, task)
            return
        }

        // Réaliser la recette une fois
        val resultWrapper = task.queuedRecipe.recipe.result.item
        if (resultWrapper is CustomComponentWrapper) {
            val product = CustomItem(
                resultWrapper.customMaterial,
                task.queuedRecipe.compoQuality,
                task.queuedRecipe.recipe.result.quantity
            )
            task.done.add(product)
        }

        // Déplacer les résultats vers le cache de collecte avec fusion
        val cache = collectionCache.getOrPut(player) { mutableMapOf() }.getOrPut(skill.name) { CollectionCache() }
        mergeIntoCache(cache.done, task.done)
        task.done.clear()
        mergeIntoCache(cache.garbage, task.garbage)
        task.garbage.clear()

        task.queuedRecipe.quantity--

        if (task.queuedRecipe.quantity <= 0) {
            finishTask(player, skill, task)
        }
    }

    private fun finishTask(player: UUID, skill: Skill, task: CraftTask) {
        craftTaskScheduler.cancel(player, skill.name)
        activeTasks[player]?.remove(skill.name)

        // Lancer la recette suivante si disponible
        val queue = queues[player]?.get(skill.name)
        if (queue != null && queue.isNotEmpty()) {
            val nextRecipe = queue.removeAt(0)
            val nextTask = CraftTask(nextRecipe, mutableListOf(), mutableListOf(), 5.seconds)
            activeTasks.getOrPut(player) { mutableMapOf() }[skill.name] = nextTask
            craftTaskScheduler.schedule(player, skill, nextTask, this)
        }
    }

}