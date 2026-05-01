package onl.tesseract.srp.controller.menu.skill

import net.kyori.adventure.text.Component
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.srp.controller.menu.ItemAdderMenu
import onl.tesseract.srp.domain.item.CustomItemIds
import onl.tesseract.srp.domain.port.PlayerInventoryPort
import onl.tesseract.srp.domain.skill.Skill
import onl.tesseract.srp.service.item.CustomItemService
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

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
                    val playerInventoryPort: PlayerInventoryPort,previous : Menu? = null): ItemAdderMenu(
    MenuSize.Six,"tesseract:recipe_advancement",skill.name, previous) {


    override fun placeButtons(viewer: Player) {
        addRecipeButton(viewer)
        addSkillTreeButton()
        addInfoButton()
        addPendingRecipeButtons()
        addCurrentRecipeButton()
        addItemToCollectButton()
        addBackButton()
        super.placeButtons(viewer)
    }

    private fun addRecipeButton(viewer: Player) {
        val item = customItemService.getCustomItem(CustomItemIds.MENU_RECIPE_BOOK_BUTTON)
        item.editMeta { it.displayName(Component.text("Recettes")) }
        addButton(RECIPE_BOOK_BUTTON_INDEX, item) {
            RecipeMenu(skill, customItemService,playerInventoryPort, this).open(viewer);
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

    private fun addPendingRecipeButtons() {
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
        pendingSlots.forEachIndexed { i, slot ->
            val item = ItemStack(Material.PAPER)
            item.editMeta { it.displayName(Component.text("Recette en attente ${i + 1}")) }
            addButton(slot, item) {}
        }
    }

    private fun addCurrentRecipeButton() {
        val item = ItemStack(Material.PAPER)
        item.editMeta { it.displayName(Component.text("Recette en cours")) }
        addButton(CURRENT_RECIPE_INDEX, item) {}
    }

    private fun addItemToCollectButton() {
        val item = ItemStack(Material.CHEST)
        item.editMeta { it.displayName(Component.text("Item à récupérer")) }
        addButton(ITEM_TO_COLLECT_INDEX, item) {}
    }


}