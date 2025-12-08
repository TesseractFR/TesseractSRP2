package onl.tesseract.srp.controller.menu.elytra

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.core.cosmetics.menu.ElytraTrailSelectionMenu
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.profile.PlayerProfileService
import onl.tesseract.srp.service.equipment.elytra.ElytraService
import org.bukkit.Material
import org.bukkit.entity.Player

private const val SLOT_FLANC_ETHERE = 22
private const val SLOT_AUTO_GLIDE = 40
private const val SLOT_PROPULSION = 4
private const val SLOT_AMELIORATIONS = 24
private const val SLOT_SILLAGE = 20

class ElytraMenu(
    private val player: Player,
    private val elytraService: ElytraService,
    private val playerProfileService: PlayerProfileService,
) : ElytraBaseMenu(MenuSize.Five, Component.text("Ailes Célestes", NamedTextColor.DARK_PURPLE)) {

    override fun placeButtons(viewer: Player) {
        val state = elytraService.getMenuState(player.uniqueId)
        if (!state.hasElytra) {
            viewer.sendMessage(Component.text("Tu ne possèdes pas d'élytra personnalisée.", NamedTextColor.RED))
            viewer.closeInventory()
            return
        }
        placeDecorations()
        placeElytraInvokeButton(viewer)
        placeAutoGlideButton(state.autoGlide, viewer)
        placePropulsionButton(viewer)
        placeUpgradeButton(viewer)
        placeSillageButton(viewer)

        addBackButton()
        addCloseButton()
    }

    private fun placeElytraInvokeButton(viewer: Player) {
        addButton(
            SLOT_FLANC_ETHERE, ItemBuilder(Material.ELYTRA)
                .name(Component.text("Flanc éthéré", NamedTextColor.LIGHT_PURPLE))
                .lore()
                .append(Component.text("Invoque ou désinvoque vos ailes divines.", NamedTextColor.GRAY))
                .buildLore()
                .build()
        ) {
            close()
            elytraService.toggleInvocation(viewer.uniqueId)
        }
    }

    private fun placeAutoGlideButton(autoGlide: Boolean, viewer: Player) {
        val autoGlideColor = if (autoGlide) NamedTextColor.GREEN else NamedTextColor.RED
        val autoGlideMaterial = if (autoGlide) Material.LIME_DYE else Material.GRAY_DYE
        addButton(
            SLOT_AUTO_GLIDE, ItemBuilder(autoGlideMaterial)
                .name(Component.text("Vol automatique", NamedTextColor.AQUA))
                .lore()
                .append(Component.text("Tombez dans le vide pour voler automatiquement", NamedTextColor.GRAY))
                .newline()
                .newline()
                .append(Component.text("Statut: ", NamedTextColor.GRAY))
                .append(Component.text(if (autoGlide) "Activé" else "Désactivé", autoGlideColor))
                .buildLore()
                .build()
        ) {
            elytraService.toggleAutoGlide(viewer.uniqueId)
            open(viewer)
        }
    }

    private fun placePropulsionButton(viewer: Player) {
        addButton(
            SLOT_PROPULSION, ItemBuilder(Material.FIREWORK_ROCKET)
                .name(Component.text("Propulsion synergique", NamedTextColor.GOLD))
                .lore()
                .append(Component.text("Vous concentrez l'énergie pour vous propulser.", NamedTextColor.BLUE))
                .buildLore()
                .build()
        ) {
            elytraService.requestPropulsion(viewer.uniqueId)
            close()
        }
    }

    private fun placeUpgradeButton(viewer: Player) {
        addButton(
            SLOT_AMELIORATIONS, ItemBuilder(Material.PRISMARINE_CRYSTALS)
                .name(Component.text("Améliorations", NamedTextColor.GOLD))
                .lore()
                .append(Component.text("Affiche les améliorations disponibles.", NamedTextColor.GRAY))
                .buildLore()
                .build()
        ) {
            ElytraUpgradeMenu(
                player.uniqueId, playerProfileService, elytraService, this)
                .open(viewer)
        }
    }

    private fun placeSillageButton(viewer: Player) {
        addButton(
            SLOT_SILLAGE, ItemBuilder(Material.NETHER_STAR)
                .name(Component.text("Sillage des ailes", NamedTextColor.DARK_AQUA))
                .lore()
                .append(Component.text("Affiche des particules pendant le vol.", NamedTextColor.GRAY))
                .buildLore()
                .build()
        ) {
            ElytraTrailSelectionMenu(viewer.uniqueId, this).open(viewer)
        }
    }
}
