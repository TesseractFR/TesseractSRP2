package onl.tesseract.srp.controller.menu.elytra

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.profile.PlayerProfileService
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.service.equipment.elytra.ElytraService
import onl.tesseract.srp.service.equipment.elytra.ElytraUpgradeEntry
import onl.tesseract.srp.service.equipment.elytra.ElytraUpgradeMenuState
import onl.tesseract.srp.service.equipment.elytra.ElytraUpgradeResult
import onl.tesseract.srp.util.ElytraChatError
import onl.tesseract.srp.util.ElytraChatFormat
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

private const val SLOT_UPGRADE_SPEED = 19
private const val SLOT_UPGRADE_PROTECTION = 21
private const val SLOT_UPGRADE_BOOST_NUMBER = 23
private const val SLOT_UPGRADE_RECOVERY = 25
private const val PLAYER_INFO_SLOT = 40

class ElytraUpgradeMenu(
    private val playerID: UUID,
    private val playerProfileService: PlayerProfileService,
    private val elytraService: ElytraService,
    previous: Menu?
) : ElytraBaseMenu(
    MenuSize.Five,
    Component.text("Améliorations Ailes Célestes", NamedTextColor.DARK_PURPLE),
    previous
) {

    override fun placeButtons(viewer: Player) {
        val state = elytraService.getUpgradeMenuState(playerID)
        if (!state.hasElytra) {
            viewer.closeInventory()
            viewer.sendMessage(ElytraChatError + "Tu ne possèdes pas d'élytra personnalisée.")
            return
        }
        placeDecorations()
        placeUpgradeButtons(viewer, state)
        placePlayerInfo(state)
        addBackButton()
        addCloseButton()
    }

    private fun placeUpgradeButtons(viewer: Player, state: ElytraUpgradeMenuState) {
        val slots = listOf(
            SLOT_UPGRADE_SPEED,
            SLOT_UPGRADE_PROTECTION,
            SLOT_UPGRADE_BOOST_NUMBER,
            SLOT_UPGRADE_RECOVERY
        )
        state.entries.forEachIndexed { index, entry ->
            val slot = slots.getOrNull(index) ?: return@forEachIndexed
            val item = buildUpgradeItem(entry)
            addButton(slot, item) {
                val result = elytraService.tryBuyNextUpgrade(playerID, entry.upgrade)
                when (result) {
                    ElytraUpgradeResult.SUCCESS -> {
                        viewer.playSound(viewer.location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                        open(viewer)
                    }
                    ElytraUpgradeResult.NOT_ENOUGH_POINTS -> {
                        viewer.sendMessage(ElytraChatError + "Tu n'as pas assez de points d'illumination.")
                    }
                    ElytraUpgradeResult.MAX_LEVEL_REACHED -> {
                        viewer.sendMessage(ElytraChatFormat + "Cette amélioration est déjà au niveau maximal.")
                    }
                    ElytraUpgradeResult.NO_ELYTRA -> {
                        viewer.sendMessage(ElytraChatError +"Tu ne possèdes plus d'élytra personnalisée.")
                        viewer.closeInventory()
                    }
                }
            }
        }
    }

    private fun buildUpgradeItem(
        entry: ElytraUpgradeEntry
    ): ItemStack {
        val builder = ItemBuilder(entry.upgrade.material)
            .name(Component.text(entry.upgrade.displayName, NamedTextColor.AQUA))
            .lore()
            .append(entry.upgrade.description, NamedTextColor.GRAY, TextDecoration.ITALIC)
            .newline().newline()
            .append("Niveau actuel : ", NamedTextColor.GREEN)
            .append("${entry.currentLevel + 1}", NamedTextColor.YELLOW)
            .newline()
            .append(elytraService.getUpgradeStatLine(entry.upgrade, entry.currentLevel), NamedTextColor.WHITE)
            .newline().newline()

        if (entry.nextLevel != null && entry.price != null && entry.currentLevel < entry.maxLevel) {
            builder.append("Prochaine amélioration :", NamedTextColor.BLUE, TextDecoration.BOLD)
                .newline()
                .append(elytraService.getUpgradeStatLine(entry.upgrade, entry.nextLevel), NamedTextColor.YELLOW)
                .newline().newline()
                .append("Coût : ", NamedTextColor.GOLD)
                .append(Component.text("${entry.price} points d'illumination",
                    if (entry.canAfford) NamedTextColor.GREEN else NamedTextColor.RED))
                .append(Component.text(" (Cliquez pour acheter)", NamedTextColor.GRAY, TextDecoration.ITALIC))
        } else {
            builder.append("Amélioration maximale atteinte", NamedTextColor.DARK_GREEN)
        }
        return builder.buildLore().build()
    }

    private fun placePlayerInfo(state: ElytraUpgradeMenuState) {
        addButton(
            PLAYER_INFO_SLOT,
            ItemBuilder(playerProfileService.getPlayerHead(playerID))
                .name(Component.text("Mes informations", NamedTextColor.GREEN))
                .lore()
                .newline()
                .addField("Argent", Component.text("${state.money} Lys", NamedTextColor.GOLD))
                .addField("Points d'illumination", Component.text("${state.illuminationPoints}", NamedTextColor.GOLD))
                .addField("Grade", Component.text(state.rankLabel, NamedTextColor.GOLD))
                .buildLore()
                .build()
        )
    }
}