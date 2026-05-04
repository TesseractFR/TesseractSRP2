package onl.tesseract.srp.service.skill

import onl.tesseract.srp.domain.item.CustomItem
import onl.tesseract.srp.domain.item.Quality
import onl.tesseract.srp.domain.port.PlayerInventoryPort
import onl.tesseract.srp.domain.skill.Skill
import onl.tesseract.srp.domain.skill.crafting.CraftTask
import onl.tesseract.srp.domain.skill.crafting.CraftingBonus
import onl.tesseract.srp.domain.skill.crafting.LootCache
import onl.tesseract.srp.domain.skill.crafting.QueuedRecipe
import onl.tesseract.srp.domain.skill.recipe.CustomComponentWrapper
import onl.tesseract.srp.domain.skill.recipe.Recipe
import onl.tesseract.srp.domain.skill.station.CraftingStation
import onl.tesseract.srp.infrastructure.scheduler.skill.CraftTaskScheduler
import onl.tesseract.srp.repository.generic.skill.CraftTaskRepository
import onl.tesseract.srp.repository.generic.skill.SkillResultCacheRepository
import onl.tesseract.srp.repository.hibernate.skill.CraftTaskEntity
import onl.tesseract.srp.repository.hibernate.skill.SkillResultCacheEntity
import onl.tesseract.srp.repository.yaml.skill.SkillConfigRepository
import onl.tesseract.srp.service.item.CustomItemService
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.springframework.stereotype.Component
import java.util.*
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

const val DEFAULT_SUCCES_RATE = 0.75f

@Component
class SkillService(val skillConfigRepository: SkillConfigRepository,
                   val customItemService: CustomItemService,
                   val playerInventoryPort: PlayerInventoryPort,
                   val craftTaskScheduler: CraftTaskScheduler,
                   val craftTaskRepository: CraftTaskRepository,
                   val skillResultCacheRepository: SkillResultCacheRepository) {


    private lateinit var activeTasks: MutableMap<UUID, MutableMap<String, CraftTask>>
    private lateinit var queues: MutableMap<UUID, MutableMap<String, MutableList<QueuedRecipe>>>
    private lateinit var lootCache: MutableMap<UUID, MutableMap<String, LootCache>>
    
    init {
        activeTasks = mutableMapOf()
        queues = mutableMapOf()
        lootCache = mutableMapOf()
        loadAllTasks()
        loadAllCollectionCaches()
    }

    private fun loadAllCollectionCaches() {
        val allCaches = skillResultCacheRepository.findAll()
        allCaches.forEach { entity ->
            val cache = lootCache.getOrPut(entity.playerUuid) { mutableMapOf() }.getOrPut(entity.skillName) { LootCache() }
            cache.done.addAll(entity.done.map { it.toDomain() })
            cache.garbage.addAll(entity.garbage.map { it.toDomain() })
        }
    }

    private fun loadAllTasks() {
        val allEntities: List<CraftTaskEntity> = craftTaskRepository.findAll()
        val skills = skillConfigRepository.getSkills()
        
        allEntities.groupBy { it.playerUuid }.forEach { (playerUuid, entities) ->
            entities.groupBy { it.skillName }.forEach { (skillName, skillEntities) ->
                val skill = skills[skillName] ?: return@forEach
                
                val activeEntity = skillEntities.find { !it.isQueue }
                val queueEntities = skillEntities.filter { it.isQueue }.sortedBy { it.queueOrder }
                
                activeEntity?.let { entity ->
                    val recipes = skill.recipe[entity.queuedRecipe.recipeTier]?.recipeByName
                    val recipe = recipes?.get(entity.queuedRecipe.recipeId)
                    if (recipe != null) {
                        val task = entity.toCraftTask(recipe)
                        activeTasks.getOrPut(playerUuid) { mutableMapOf() }[skillName] = task
                        craftTaskScheduler.schedule(playerUuid, skill, task, this)
                    }
                }
                
                queueEntities.forEach { entity ->
                    val recipes = skill.recipe[entity.queuedRecipe.recipeTier]?.recipeByName
                    val recipe = recipes?.get(entity.queuedRecipe.recipeId)
                    if (recipe != null) {
                        val queuedRecipe = entity.queuedRecipe.toDomain(recipe)
                        queues.getOrPut(playerUuid) { mutableMapOf() }.getOrPut(skillName) { mutableListOf() }.add(queuedRecipe)
                    }
                }
            }
        }
    }




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
        val cache = lootCache[player]?.get(skill.name) ?: return

        val itemsToGive = mutableListOf<ItemStack>()

        cache.done.forEach { itemsToGive.add(customItemService.toItemstack(it)) }
        cache.garbage.forEach { itemsToGive.add(customItemService.toItemstack(it)) }

        if (itemsToGive.isEmpty()) return

        playerInventoryPort.giveItems(player, itemsToGive)
        cache.done.clear()
        cache.garbage.clear()
        saveCollectionCache(player, skill.name)

        Bukkit.getPlayer(player)?.sendMessage("§aVous avez récupéré vos objets !")
    }

    private fun saveCollectionCache(player: UUID, skillName: String) {
        val cache = lootCache[player]?.get(skillName) ?: return
        skillResultCacheRepository.deleteByPlayerAndSkill(player, skillName)
        if (cache.done.isNotEmpty() || cache.garbage.isNotEmpty()) {
            val entity = SkillResultCacheEntity(
                playerUuid = player,
                skillName = skillName,
                done = cache.done.map { onl.tesseract.srp.repository.hibernate.skill.CustomItemEmbeddable.fromDomain(it) }.toMutableList(),
                garbage = cache.garbage.map { onl.tesseract.srp.repository.hibernate.skill.CustomItemEmbeddable.fromDomain(it) }.toMutableList()
            )
            skillResultCacheRepository.save(entity)
        }
    }

    fun hasItemsToCollect(player: UUID, skillName: String): Boolean {
        val cache = lootCache[player]?.get(skillName) ?: return false
        return cache.done.isNotEmpty() || cache.garbage.isNotEmpty()
    }

    fun getItemsToCollect(player: UUID, skillName: String): LootCache {
        val cache = lootCache[player]?.get(skillName)?: LootCache()
        return LootCache(cache.done.toMutableList(), cache.garbage.toMutableList())
    }

    fun startCraft(player: UUID, skill: Skill, recipe: Recipe, quantity: Int, station: CraftingStation = CraftingStation()) {
        // ... (vérifications inchangées)
        if (recipe.tier > station.tier) {
            Bukkit.getPlayer(player)?.sendMessage("§cLe niveau de cette table est insuffisant pour cette recette ! (Tier requis : ${recipe.tier})")
            return
        }

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
        
        val bonus = CraftingBonus(
            qualityBonus = station.qualityBonus,
            successBonus = station.successBonus
        )

        if (activeTasks[player]?.get(skill.name) == null) {
            val craftTask = CraftTask(queuedRecipe, mutableListOf(), mutableListOf(), 5.seconds, bonus)
            activeTasks.getOrPut(player) { mutableMapOf() }[skill.name] = craftTask
            craftTaskScheduler.schedule(player, skill, craftTask, this)
            Bukkit.getPlayer(player)?.sendMessage("§aFabrication lancée !")
            saveTasks(player, skill.name)
        } else {
            queue.add(queuedRecipe)
            Bukkit.getPlayer(player)?.sendMessage("§aRecette ajoutée à la file d'attente !")
            saveTasks(player, skill.name)
        }
    }

    private fun saveTasks(player: UUID, skillName: String) {
        craftTaskRepository.deleteByPlayerAndSkill(player, skillName)
        val entities = mutableListOf<CraftTaskEntity>()

        activeTasks[player]?.get(skillName)?.let { task ->
            val recipeId = task.queuedRecipe.recipe.name
            entities.add(CraftTaskEntity.fromDomain(player, skillName, task, recipeId))
        }
        
        queues[player]?.get(skillName)?.forEachIndexed { index, queuedRecipe ->
            val bonus = activeTasks[player]?.get(skillName)?.bonus ?: CraftingBonus()
            val recipeId = queuedRecipe.recipe.name
            entities.add(CraftTaskEntity.fromQueuedRecipe(player, skillName, queuedRecipe, recipeId, index, bonus))
        }
        
        if (entities.isNotEmpty()) {
            craftTaskRepository.saveAll(entities)
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

        val bonus = task.bonus

        // Appliquer le taux de réussite
        val effectiveSuccessRate = DEFAULT_SUCCES_RATE + bonus.successBonus
        val success = Random.nextDouble() <= effectiveSuccessRate

        if (success) {
            // Réaliser la recette une fois
            val resultWrapper = task.queuedRecipe.recipe.result.item
            if (resultWrapper is CustomComponentWrapper) {
                // Appliquer le bonus de qualité
                var quality = task.queuedRecipe.compoQuality
                if (Random.nextDouble() <= bonus.qualityBonus) {
                    quality = quality.next()
                }

                val product = CustomItem(
                    resultWrapper.customMaterial,
                    quality,
                    task.queuedRecipe.recipe.result.quantity
                )
                task.done.add(product)
            }
        } else {
            // Échec du craft - on pourrait ajouter des résidus (garbage) ici si nécessaire
            Bukkit.getPlayer(player)?.sendMessage("§cÉchec de la fabrication d'une unité !")
        }

        // Déplacer les résultats vers le cache de collecte avec fusion
        val cache = lootCache.getOrPut(player) { mutableMapOf() }.getOrPut(skill.name) { LootCache() }
        mergeIntoCache(cache.done, task.done)
        task.done.clear()
        mergeIntoCache(cache.garbage, task.garbage)
        task.garbage.clear()

        task.queuedRecipe.quantity--
        saveTasks(player, skill.name)
        saveCollectionCache(player, skill.name)

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
            val nextTask = CraftTask(nextRecipe, mutableListOf(), mutableListOf(), 5.seconds, task.bonus)
            activeTasks.getOrPut(player) { mutableMapOf() }[skill.name] = nextTask
            craftTaskScheduler.schedule(player, skill, nextTask, this)
        }
        saveTasks(player, skill.name)
    }

}