package onl.tesseract.srp.controller.menu.skill

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.srp.controller.menu.ItemAdderMenu
import onl.tesseract.srp.domain.item.CustomItemIds
import onl.tesseract.srp.domain.skill.Skill
import onl.tesseract.srp.service.item.CustomItemService
import onl.tesseract.srp.service.skill.SkillService
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

private const val TIER_BUTTON_INDEX = 10
private const val QUALITY_BUTTON_INDEX = 12
private const val SUCCESS_BUTTON_INDEX = 14
private const val TIME_BUTTON_INDEX = 16

private const val RECOVERY_SUCCESS_INDEX = 29
private const val RECOVERY_FAILURE_INDEX = 31
private const val MULTI_CRAFT_INDEX = 33

private const val BACK_BUTTON_INDEX = 36
private const val INFO_BUTTON_INDEX = 44

class SkillUpgradeMenu(
    val skill: Skill,
    val customItemService: CustomItemService,
    val skillService: SkillService,
    previous: Menu? = null
) : ItemAdderMenu(MenuSize.Five, "tesseract:skill_upgrade", "", previous) {

    override fun placeButtons(viewer: Player) {
        addTierButton()
        addQualityButton()
        addSuccessButton()
        addTimeButton()

        addRecoverySuccessButton()
        addRecoveryFailureButton()
        addMultiCraftButton()

        addButton(BACK_BUTTON_INDEX,customItemService.getCustomItem(CustomItemIds.MENU_BACK_ARROW_BUTTON)){
            if(previous==null){
                this.close()
                return@addButton
            }
            previous?.open(viewer)
        }
        addInfoButton()
    }

    private fun addTierButton() {
        val item = ItemStack(Material.ENCHANTED_BOOK)
        item.editMeta { meta ->
            meta.displayName(
                Component.text("+1 Tier de recettes", NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false)
            )
        }
        addButton(TIER_BUTTON_INDEX, item) {}
    }

    private fun addQualityButton() {
        val item = ItemStack(Material.AMETHYST_SHARD)
        item.editMeta { meta ->
            meta.displayName(
                Component.text("+x% Qualité des items craftés", NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false)
            )
            meta.lore(listOf(
                Component.text("Augmente la chance d'obtenir une ", NamedTextColor.GRAY)
                    .append(Component.text("qualité plus élevée", NamedTextColor.WHITE))
                    .append(Component.text(".", NamedTextColor.GRAY))
            ))
        }
        addButton(QUALITY_BUTTON_INDEX, item) {}
    }

    private fun addSuccessButton() {
        val item = ItemStack(Material.RABBIT_FOOT)
        item.editMeta { meta ->
            meta.displayName(
                Component.text("+x% Chance de succès", NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false)
            )
            meta.lore(listOf(
                Component.text("Augmente la ", NamedTextColor.GRAY)
                    .append(Component.text("probabilité de réussite", NamedTextColor.WHITE))
                    .append(Component.text(" du craft.", NamedTextColor.GRAY))
            ))
        }
        addButton(SUCCESS_BUTTON_INDEX, item) {}
    }

    private fun addTimeButton() {
        val item = ItemStack(Material.CLOCK)
        item.editMeta { meta ->
            meta.displayName(
                Component.text("-x% Temps de craft", NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false)
            )
            meta.lore(listOf(
                Component.text("Réduit le ", NamedTextColor.GRAY)
                    .append(Component.text("temps de fabrication", NamedTextColor.WHITE))
                    .append(Component.text(" par recette.", NamedTextColor.GRAY))
            ))
        }
        addButton(TIME_BUTTON_INDEX, item) {}
    }

    private fun addRecoverySuccessButton() {
        val item = ItemStack(Material.EMERALD)
        item.editMeta { meta ->
            meta.displayName(
                Component.text("+x% Récupération (succès)", NamedTextColor.GREEN)
                    .decoration(TextDecoration.ITALIC, false)
            )
            meta.lore(listOf(
                Component.text("Récupère une partie des ", NamedTextColor.GRAY)
                    .append(Component.text("composants utilisés", NamedTextColor.WHITE))
                    .append(Component.text(" en cas de ", NamedTextColor.GRAY))
                    .append(Component.text("réussite", NamedTextColor.GREEN))
                    .append(Component.text(".", NamedTextColor.GRAY))
            ))
        }
        addButton(RECOVERY_SUCCESS_INDEX, item) {}
    }

    private fun addRecoveryFailureButton() {
        val item = ItemStack(Material.FLINT)
        item.editMeta { meta ->
            meta.displayName(
                Component.text("+x% Récupération (échec)", NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false)
            )
            meta.lore(listOf(
                Component.text("Récupère une partie des ", NamedTextColor.GRAY)
                    .append(Component.text("composants utilisés", NamedTextColor.WHITE))
                    .append(Component.text(" en cas d'", NamedTextColor.GRAY))
                    .append(Component.text("échec", NamedTextColor.RED))
                    .append(Component.text(".", NamedTextColor.GRAY))
            ))
        }
        addButton(RECOVERY_FAILURE_INDEX, item) {}
    }

    private fun addMultiCraftButton() {
        val item = ItemStack(Material.BUNDLE)
        item.editMeta { meta ->
            meta.displayName(
                Component.text("+x% Craft multiple", NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false)
            )
            meta.lore(listOf(
                Component.text("Chance de produire ", NamedTextColor.GRAY)
                    .append(Component.text("plusieurs exécutions", NamedTextColor.WHITE))
                    .append(Component.text(" d'une même recette simultanément.", NamedTextColor.GRAY))
            ))
        }
        addButton(MULTI_CRAFT_INDEX, item) {}
    }

    private fun addInfoButton() {
        val item = customItemService.getCustomItem(CustomItemIds.MENU_INFORMATION_BUTTON)
        item.editMeta { it.displayName(Component.text("Informations")) }
        addButton(INFO_BUTTON_INDEX, item) {}
    }
}