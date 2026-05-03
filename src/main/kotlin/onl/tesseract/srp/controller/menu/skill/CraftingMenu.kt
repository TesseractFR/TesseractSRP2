package onl.tesseract.srp.controller.menu.skill

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.srp.controller.menu.ItemAdderMenu
import onl.tesseract.srp.domain.item.CustomItemIds
import onl.tesseract.srp.domain.port.PlayerInventoryPort
import onl.tesseract.srp.domain.skill.recipe.Recipe
import onl.tesseract.srp.domain.skill.Skill
import onl.tesseract.srp.domain.skill.recipe.ComponentWrapper
import onl.tesseract.srp.domain.skill.recipe.CustomComponentWrapper
import onl.tesseract.srp.domain.skill.recipe.VanillaComponentWrapper
import onl.tesseract.srp.service.item.CustomItemService
import onl.tesseract.srp.service.skill.RecipeService
import onl.tesseract.srp.service.skill.SkillService
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.math.max
import kotlin.math.min

private const val COMPONENTS_OFFSET = -1
private const val RESULT_INDEX = 8

private const val INFO_BUTTON_INDEX = 18

private const val QUANTITY_PLUS_FIRST_INDEX = 21
private const val QUANTITY_PLUS_SECOND_INDEX = 22
private const val QUANTITY_MAX_INDEX = 23

private const val QUANTITY_DISPLAY_INDEX = 31
private const val LAUNCH_BUTTON_INDEX = 34

private const val RECIPE_BOOK_BUTTON_INDEX = 36
private const val QUANTITY_MINUS_FIRST_INDEX = 39
private const val QUANTITY_MINUS_SECOND_INDEX = 40
private const val QUANTITY_MIN_INDEX = 41

private const val FIRST_OFFSET = 1
private const val SECOND_OFFSET = 5

class CraftingMenu(val skill : Skill,
                   val customItemService: CustomItemService,
                   val recipeService: RecipeService,
                   val skillService: SkillService,
                   val playerInventoryPort: PlayerInventoryPort,
                   val activeRecipe: Recipe ,previous : Menu? = null) : ItemAdderMenu(
    MenuSize.Six,"tesseract:recipe_launch","",
    previous){

    private var quantityToCraft = 1

    override fun placeButtons(viewer: Player) {
        addRecipeButton()
        addActiveRecipe()
        addInfoButton()
        addLaunchButton(viewer)
        addQuantityButtons(viewer)
    }

    private fun addQuantityButtons(viewer: Player) {
        val maxCraft = getMaxCraft(viewer)
        addMinButton(viewer)
        addMinusButton(viewer, SECOND_OFFSET, QUANTITY_MINUS_SECOND_INDEX, CustomItemIds.MENU_MINUS_5_BUTTON)
        addMinusButton(viewer, FIRST_OFFSET, QUANTITY_MINUS_FIRST_INDEX, CustomItemIds.MENU_MINUS_1_BUTTON)
        addCurrentQuantityButton()
        addPlusButton(viewer, FIRST_OFFSET, QUANTITY_PLUS_FIRST_INDEX, CustomItemIds.MENU_PLUS_1_BUTTON, maxCraft)
        addPlusButton(viewer, SECOND_OFFSET, QUANTITY_PLUS_SECOND_INDEX, CustomItemIds.MENU_PLUS_5_BUTTON, maxCraft)
        addMaxButton(viewer, maxCraft)
    }

    private fun addCurrentQuantityButton() {
        addButton(
            QUANTITY_DISPLAY_INDEX,
            customItemService.getCustomItem(CustomItemIds.MENU_QUANTITY_BUTTON).also {
                it.editMeta { m ->
                    m.displayName(Component.text("Nombre actuel d'exécutions : x$quantityToCraft", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                    m.lore(listOf(
                        Component.text("La recette sera lancée ", NamedTextColor.GRAY)
                            .append(Component.text("$quantityToCraft fois", NamedTextColor.WHITE))
                            .append(Component.text(".", NamedTextColor.GRAY))
                    ))
                }
            }.asQuantity(quantityToCraft)
        )
    }

    private fun addMinButton(viewer: Player) {
        val canMin = quantityToCraft > 1
        addButton(QUANTITY_MIN_INDEX, customItemService.getCustomItem(CustomItemIds.MENU_MIN_BUTTON).also {
            it.editMeta { m ->
                m.displayName(Component.text("MIN", if (canMin) NamedTextColor.GREEN else NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
                m.lore(listOf(
                    if (canMin)
                        Component.text("Retire ", NamedTextColor.GRAY)
                            .append(Component.text("${quantityToCraft - 1} fois", NamedTextColor.GREEN))
                            .append(Component.text(" la recette.", NamedTextColor.GRAY))
                    else
                        Component.text("Nombre d'exécutions déjà au minimum.", NamedTextColor.GRAY),
                    if (canMin)
                        Component.text("Exécution(s) après retrait : ", NamedTextColor.GRAY).append(Component.text("x1", NamedTextColor.GREEN))
                    else
                        Component.text("Exécution(s) minimum : ", NamedTextColor.GRAY).append(Component.text("x1", NamedTextColor.RED)),
                ))
            }
        }) {
            quantityToCraft = 1
            this.placeButtons(viewer)
        }
    }

    private fun addMinusButton(viewer: Player, offset: Int, index: Int, itemId: String) {
        val newQuantity = max(1, quantityToCraft - offset)
        val canMinus = quantityToCraft - offset >= 1
        addButton(index, customItemService.getCustomItem(itemId).also {
            it.editMeta { m ->
                m.displayName(Component.text("-$offset", if (canMinus) NamedTextColor.GREEN else NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
                m.lore(listOf(
                    if (canMinus)
                        Component.text("Retire ", NamedTextColor.GRAY)
                            .append(Component.text("$offset fois", NamedTextColor.GREEN))
                            .append(Component.text(" la recette.", NamedTextColor.GRAY))
                    else
                        Component.text("Impossible de retirer $offset fois.", NamedTextColor.RED),
                    if (canMinus)
                        Component.text("Exécution(s) après retrait : ", NamedTextColor.GRAY).append(Component.text("x$newQuantity", NamedTextColor.GREEN))
                    else
                        Component.text("Exécution(s) minimum : ", NamedTextColor.GRAY).append(Component.text("x1", NamedTextColor.RED)),
                ))
            }
        }) {
            if (canMinus) {
                quantityToCraft = newQuantity
                this.placeButtons(viewer)
            }
        }
    }

    private fun buildPlusButtonLore(offset: Int, maxCraft: Int, canPlus: Boolean, alreadyMax: Boolean, noMaterials: Boolean, newQuantity: Int): List<Component> {
        return listOf(
            when {
                noMaterials -> Component.text("Pas assez de matériaux.", NamedTextColor.RED)
                alreadyMax -> Component.text("Nombre d'exécutions déjà au maximum.", NamedTextColor.GRAY)
                !canPlus -> Component.text("Impossible d'ajouter $offset fois, maximum dépassé.", NamedTextColor.RED)
                else -> Component.text("Ajoute ", NamedTextColor.GRAY)
                    .append(Component.text("$offset fois", NamedTextColor.GREEN))
                    .append(Component.text(" la recette.", NamedTextColor.GRAY))
            },
            when {
                canPlus -> Component.text("Exécution(s) après ajout : ", NamedTextColor.GRAY).append(Component.text("x$newQuantity", NamedTextColor.GREEN))
                else -> Component.text("Exécution(s) maximum : ", NamedTextColor.GRAY).append(Component.text("x$maxCraft", NamedTextColor.RED))
            },
        )
    }

    private fun addPlusButton(viewer: Player, offset: Int, index: Int, itemId: String, maxCraft: Int) {
        val noMaterials = maxCraft == 0
        val canPlus = !noMaterials && quantityToCraft + offset <= maxCraft
        val alreadyMax = !noMaterials && quantityToCraft >= maxCraft
        val newQuantity = if (noMaterials) 1 else min(maxCraft, quantityToCraft + offset)
        addButton(index, customItemService.getCustomItem(itemId).also {
            it.editMeta { m ->
                m.displayName(Component.text("+$offset", if (canPlus) NamedTextColor.GREEN else NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
                m.lore(buildPlusButtonLore(offset, maxCraft, canPlus, alreadyMax, noMaterials, newQuantity))
            }
        }) {
            if (canPlus) {
                quantityToCraft = newQuantity
                this.placeButtons(viewer)
            }
        }
    }

    private fun addMaxButton(viewer: Player, maxCraft: Int) {
        val noMaterials = maxCraft == 0
        val canMax = !noMaterials && maxCraft > quantityToCraft
        val alreadyMax = !noMaterials && quantityToCraft >= maxCraft
        addButton(QUANTITY_MAX_INDEX, customItemService.getCustomItem(CustomItemIds.MENU_MAX_BUTTON).also {
            it.editMeta { m ->
                m.displayName(Component.text("MAX", if (canMax) NamedTextColor.GREEN else NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
                m.lore(listOf(
                    if (noMaterials)
                        Component.text("Pas assez de matériaux.", NamedTextColor.RED)
                    else if (alreadyMax)
                        Component.text("Nombre d'exécutions déjà au maximum.", NamedTextColor.GRAY)
                    else
                        Component.text("Ajoute ", NamedTextColor.GRAY)
                            .append(Component.text("${maxCraft - quantityToCraft} fois", NamedTextColor.GREEN))
                            .append(Component.text(" la recette.", NamedTextColor.GRAY)),
                    if (canMax)
                        Component.text("Exécution(s) après ajout : ", NamedTextColor.GRAY).append(Component.text("x$maxCraft", NamedTextColor.GREEN))
                    else
                        Component.text("Exécution(s) maximum : ", NamedTextColor.GRAY).append(Component.text("x$maxCraft", NamedTextColor.RED)),
                ))
            }
        }) {
            if (canMax) {
                quantityToCraft = maxCraft
                this.placeButtons(viewer)
            }
        }
    }

    private fun addActiveRecipe() {
        activeRecipe.components.forEach { (i, component) ->
            addButton(COMPONENTS_OFFSET+i,
                customItemService.toItemstack(component.item)
                        .asQuantity(component.quantity*quantityToCraft))
        }
        addButton(RESULT_INDEX, customItemService.toItemstack(activeRecipe.result.item)
                .asQuantity(activeRecipe.result.quantity*quantityToCraft))
    }

    private fun addInfoButton() {
        val item = customItemService.getCustomItem(CustomItemIds.MENU_INFORMATION_BUTTON)
        item.editMeta { meta ->
            meta.displayName(
                Component.text("Informations", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false)
            )
            meta.lore(listOf(
                Component.empty(),
                Component.text("Recette sélectionnée", NamedTextColor.YELLOW),
                Component.text("La ligne du haut affiche les ", NamedTextColor.GRAY)
                    .append(Component.text("matériaux ", NamedTextColor.WHITE)),
                Component.text("nécessaires ", NamedTextColor.WHITE)
                    .append(Component.text("et le ", NamedTextColor.GRAY))
                    .append(Component.text("résultat", NamedTextColor.WHITE))
                    .append(Component.text(" de la recette.", NamedTextColor.GRAY)),
                Component.empty(),
                Component.text("Quantité", NamedTextColor.YELLOW),
                Component.text("Utilisez ", NamedTextColor.GRAY)
                    .append(Component.text("+1, +5, MAX", NamedTextColor.GREEN))
                    .append(Component.text(" pour augmenter,", NamedTextColor.GRAY)),
                Component.text("et ", NamedTextColor.GRAY)
                    .append(Component.text("-1, -5, MIN", NamedTextColor.RED))
                    .append(Component.text(" pour diminuer le nombre de fois", NamedTextColor.GRAY)),
                Component.text("que la recette sera effectuée.", NamedTextColor.GRAY),
                Component.text("La ", NamedTextColor.GRAY)
                    .append(Component.text("balance", NamedTextColor.WHITE))
                    .append(Component.text(" affiche le nombre actuel d'exécutions.", NamedTextColor.GRAY)),
                Component.empty(),
                Component.text("Le maximum est atteint quand un matériau", NamedTextColor.GRAY),
                Component.text("dépasse ", NamedTextColor.GRAY)
                    .append(Component.text("1 stack (64 unités)", NamedTextColor.WHITE))
                    .append(Component.text(".", NamedTextColor.GRAY)),
                Component.empty(),
                Component.text("Lancement", NamedTextColor.YELLOW),
                Component.text("Cliquez sur ", NamedTextColor.GRAY)
                    .append(Component.text("l'enclume", NamedTextColor.WHITE).decorate(TextDecoration.BOLD))
                    .append(Component.text(" pour ajouter la recette ", NamedTextColor.GRAY)),
                Component.text("dans la file d'attente de la ", NamedTextColor.GRAY)
                    .append(Component.text("table d'artisanat", NamedTextColor.WHITE))
                    .append(Component.text(".", NamedTextColor.GRAY)),
            ))
        }
        addButton(INFO_BUTTON_INDEX, item) {}
    }

    private fun addRecipeButton() {
        val item = customItemService.getCustomItem(CustomItemIds.MENU_RECIPE_BOOK_BUTTON)
        item.editMeta { meta ->
            meta.displayName(Component.text("Livre de recettes").decoration(TextDecoration.ITALIC, false))
            meta.lore(listOf(
                Component.text("Retour vers la sélection de recettes.", NamedTextColor.GRAY),
            ))
        }
        addButton(RECIPE_BOOK_BUTTON_INDEX, item) { event ->
            RecipeMenu(skill, customItemService, playerInventoryPort, recipeService, skillService, this).open(event.whoClicked as Player)
        }
    }

    private fun addLaunchButton(viewer: Player) {
        val maxCraft = getMaxCraft(viewer)
        val canLaunch = quantityToCraft <= maxCraft && maxCraft > 0
        val resultColor = if (canLaunch) NamedTextColor.GREEN else NamedTextColor.RED
        val item = ItemStack(Material.ANVIL)
        item.editMeta { meta ->
            meta.displayName(Component.text("Lancer la fabrication", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false))
            meta.lore(listOf(
                Component.text("x$quantityToCraft ", NamedTextColor.WHITE)
                    .append(Component.text("la recette → ", NamedTextColor.GRAY))
                    .append(Component.text("${activeRecipe.result.quantity * quantityToCraft}x ${getComponentName(activeRecipe.result.item)}", resultColor)),
                Component.empty(),
                if (canLaunch)
                    Component.text("Ajoute à la file d'attente.", NamedTextColor.GRAY)
                else
                    Component.text("Pas assez de matériaux.", NamedTextColor.RED),
            ))
        }
        addButton(LAUNCH_BUTTON_INDEX, item) { event ->
            if (!canLaunch) return@addButton
            val player = event.whoClicked as Player
            skillService.startCraft(player.uniqueId, skill, activeRecipe, quantityToCraft)
            SkillMainMenu(skill, customItemService, recipeService, skillService, playerInventoryPort, this).open(viewer)
        }
    }

    private fun getComponentName(wrapper: ComponentWrapper): String = when (wrapper) {
        is CustomComponentWrapper -> wrapper.customMaterial.displayName
        is VanillaComponentWrapper -> wrapper.material.name.lowercase().replaceFirstChar { it.uppercase() }
        else -> "?"
    }

    private fun getMaxCraft(viewer: Player): Int {
        var max = Int.MAX_VALUE
        activeRecipe.components.forEach { (_, component) ->
            val owned = playerInventoryPort.getItemNumber(
                viewer.uniqueId,
                customItemService.toItemstack(component.item)
            )
            val craftable = owned / component.quantity
            max = min(max, craftable)
        }
        if (max == Int.MAX_VALUE) return 0
        return min(max, recipeService.getMaxStackSize(activeRecipe))
    }

}