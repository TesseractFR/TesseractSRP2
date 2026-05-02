package onl.tesseract.srp.controller.menu.skill

import net.kyori.adventure.text.Component
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.srp.controller.menu.ItemAdderMenu
import onl.tesseract.srp.domain.item.CustomItemIds
import onl.tesseract.srp.domain.port.PlayerInventoryPort
import onl.tesseract.srp.domain.skill.Skill
import onl.tesseract.srp.service.item.CustomItemService
import onl.tesseract.srp.service.skill.RecipeService
import onl.tesseract.srp.service.skill.SkillService
import onl.tesseract.srp.PLUGIN_INSTANCE
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

private const val RECIPE_BOOK_BUTTON_INDEX = 0
private const val SKILL_TREE_BUTTON_INDEX = 4
private const val INFO_BUTTON_INDEX = 8

private const val PENDING_RECIPE_1_INDEX = 19
private const val PENDING_RECIPE_2_INDEX = 20
private const val PENDING_RECIPE_3_INDEX = 21
private const val PENDING_RECIPE_4_INDEX = 22
private const val PENDING_RECIPE_5_INDEX = 23
private const val PENDING_RECIPE_6_INDEX = 24
private const val PENDING_RECIPE_7_INDEX = 25
private const val PENDING_RECIPE_8_INDEX = 26

private const val CURRENT_RECIPE_INDEX = 39
private const val ITEM_TO_COLLECT_INDEX = 41

class SkillMainMenu(val skill : Skill,
                    val customItemService: CustomItemService,
                    val recipeService: RecipeService,
                    val skillService: SkillService,
                    val playerInventoryPort: PlayerInventoryPort,previous : Menu? = null): ItemAdderMenu(
    MenuSize.Six,"tesseract:recipe_advancement",skill.name, previous) {

    private var refreshTask: BukkitTask? = null

    override fun open(viewer: Player) {
        super.open(viewer)
        refreshTask = object : BukkitRunnable() {
            override fun run() {
                if (!hasViewer()) {
                    cancel()
                    return
                }
                refresh(viewer)
            }
        }.runTaskTimer(PLUGIN_INSTANCE, 20L, 20L)
    }

    override fun onClose(event: InventoryCloseEvent) {
        super.onClose(event)
        refreshTask?.cancel()
    }


    override fun placeButtons(viewer: Player) {
        addRecipeButton(viewer)
        addSkillTreeButton()
        addInfoButton()
        addPendingRecipeButtons(viewer)
        addCurrentRecipeButton(viewer)
        addItemToCollectButton(viewer)
        addBackButton()
        super.placeButtons(viewer)
    }

    private fun addRecipeButton(viewer: Player) {
        val item = customItemService.getCustomItem(CustomItemIds.MENU_RECIPE_BOOK_BUTTON)
        item.editMeta { it.displayName(Component.text("Recettes")) }
        addButton(RECIPE_BOOK_BUTTON_INDEX, item) {
            RecipeMenu(skill, customItemService, playerInventoryPort, recipeService, skillService, this).open(viewer);
        }
    }

    private fun addSkillTreeButton() {
        val item = ItemStack(Material.EXPERIENCE_BOTTLE)
        item.editMeta { it.displayName(Component.text("Arbre d'améliorations")) }
        addButton(SKILL_TREE_BUTTON_INDEX, item) {}
    }

    private fun addInfoButton() {
        val item = customItemService.getCustomItem(CustomItemIds.MENU_INFORMATION_BUTTON)
        item.editMeta { it.displayName(Component.text("Informations")) }
        addButton(INFO_BUTTON_INDEX, item) {}
    }

    private fun addPendingRecipeButtons(viewer: Player) {
        val pendingSlots = listOf(
            PENDING_RECIPE_1_INDEX,
            PENDING_RECIPE_2_INDEX,
            PENDING_RECIPE_3_INDEX,
            PENDING_RECIPE_4_INDEX,
            PENDING_RECIPE_5_INDEX,
            PENDING_RECIPE_6_INDEX,
            PENDING_RECIPE_7_INDEX,
            PENDING_RECIPE_8_INDEX,
        )
        val queue = skillService.getQueue(viewer.uniqueId, skill.name)

        pendingSlots.forEachIndexed { i, slot ->
            val queuedRecipe = queue.getOrNull(i)
            val item = if (queuedRecipe != null) {
                val resultItem = customItemService.toItemstack(queuedRecipe.recipe.result.item)
                resultItem.asQuantity(maxOf(1, queuedRecipe.quantity))
                resultItem.editMeta { meta ->
                    meta.displayName(Component.text("§eRecette en attente : ${queuedRecipe.recipe.result.quantity} x ..."))
                    val lore = mutableListOf<Component>()
                    lore.add(Component.text("§7Quantité : §f${queuedRecipe.quantity}"))
                    meta.lore(lore)
                }
                resultItem
            } else {
                ItemStack(Material.PAPER).also {
                    it.editMeta { meta -> meta.displayName(Component.text("Slot de file d'attente vide")) }
                    it.amount = 0
                }
            }
            addButton(slot, item) {}
        }
    }

    private fun addCurrentRecipeButton(viewer: Player) {
        val task = skillService.getActiveTask(viewer.uniqueId, skill.name)
        val item = if (task != null) {
            val resultItem = customItemService.toItemstack(task.queuedRecipe.recipe.result.item)
            resultItem.asQuantity(maxOf(1, task.queuedRecipe.quantity))
            resultItem.editMeta { meta ->
                meta.displayName(Component.text("§aFabrication en cours..."))
                val lore = mutableListOf<Component>()
                lore.add(Component.text("§7Quantité restante : §f${task.queuedRecipe.quantity}"))
                lore.add(Component.text("§7Temps restant : §f${task.getTotalTimeLeft()}"))
                meta.lore(lore)
            }
            resultItem
        } else {
            ItemStack(Material.BARRIER).also {
                it.editMeta { meta -> meta.displayName(Component.text("Aucune fabrication en cours")) }
            }
        }
        addButton(CURRENT_RECIPE_INDEX, item) {}
    }

    private fun addItemToCollectButton(viewer: Player) {
        val hasItems = skillService.hasItemsToCollect(viewer.uniqueId, skill.name)

        val item = if (hasItems) {
            val (doneCount, garbageCount) = skillService.getItemsToCollectCount(viewer.uniqueId, skill.name)
            ItemStack(Material.CHEST).also {
                it.editMeta { meta ->
                    meta.displayName(Component.text("§aObjets à récupérer"))
                    val lore = mutableListOf<Component>()
                    if (doneCount > 0) lore.add(Component.text("§7- $doneCount types d'objets fabriqués"))
                    if (garbageCount > 0) lore.add(Component.text("§7- $garbageCount résidus"))
                    meta.lore(lore)
                }
            }
        } else {
            ItemStack(Material.MINECART).also {
                it.editMeta { meta -> meta.displayName(Component.text("§cRien à récupérer")) }
            }
        }

        addButton(ITEM_TO_COLLECT_INDEX, item) {
            skillService.collectCraftResults(viewer.uniqueId, skill)
            this.placeButtons(viewer)
        }
    }


}