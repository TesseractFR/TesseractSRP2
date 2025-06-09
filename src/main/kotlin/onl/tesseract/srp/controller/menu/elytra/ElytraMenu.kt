package onl.tesseract.srp.controller.menu.elytra

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.core.cosmetics.menu.ElytraTrailSelectionMenu
import onl.tesseract.lib.equipment.EquipmentService
import onl.tesseract.lib.event.equipment.invocable.Elytra
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.profile.PlayerProfileService
import onl.tesseract.srp.service.elytra.ElytraService
import onl.tesseract.srp.service.player.SrpPlayerService
import org.bukkit.Material
import org.bukkit.entity.Player

private const val SLOT_FLANC_ETHERE = 22
private const val SLOT_AUTO_GLIDE = 40
private const val SLOT_PROPULSION = 4
private const val SLOT_AMELIORATIONS = 24
private const val SLOT_SILLAGE = 20

class ElytraMenu(
    private val player: Player,
    private val equipmentService: EquipmentService,
    private val elytraService: ElytraService,
    private val playerService: SrpPlayerService,
    private val playerProfileService: PlayerProfileService,
) : Menu(MenuSize.Five, Component.text("Ailes Célestes", NamedTextColor.DARK_PURPLE)) {

    override fun placeButtons(viewer: Player) {
        val equipment = equipmentService.getEquipment(player.uniqueId)
        val elytra = equipment.get(Elytra::class.java)
        if (elytra == null) {
            viewer.sendMessage(Component.text("Tu ne possèdes pas d'élytra personnalisée.", NamedTextColor.RED))
            viewer.closeInventory()
            return
        }

        placeElytraInvokeButton(elytra, viewer)
        placeAutoGlideButton(elytra, viewer)
        placePropulsionButton(elytra, viewer)
        placeUpgradeButton(viewer)
        placeSillageButton(viewer)
        placeDecorations()

        addBackButton()
        addCloseButton()
    }

    private fun placeElytraInvokeButton(elytra: Elytra, viewer: Player) {
        addButton(
            SLOT_FLANC_ETHERE, ItemBuilder(Material.ELYTRA)
                .name(Component.text("Flanc éthéré", NamedTextColor.LIGHT_PURPLE))
                .lore()
                .append(Component.text("Invoque ou désinvoque vos ailes divines.", NamedTextColor.GRAY))
                .buildLore()
                .build()
        ) {
            close()
            if (elytra.isInvoked) {
                elytra.onUninvoke(viewer, true)
            } else if (elytra.canInvoke(viewer)) {
                elytra.onInvoke(viewer, true)
            } else {
                viewer.sendMessage(
                    Component.text(
                        "Vous devez libérer votre plastron pour invoquer vos ailes.",
                        NamedTextColor.RED
                    )
                )
            }
        }
    }

    private fun placeAutoGlideButton(elytra: Elytra, viewer: Player) {
        val isAutoGlide = elytra.autoGlide
        val autoGlideColor = if (isAutoGlide) NamedTextColor.GREEN else NamedTextColor.RED
        val autoGlideMaterial = if (isAutoGlide) Material.LIME_DYE else Material.GRAY_DYE
        addButton(
            SLOT_AUTO_GLIDE, ItemBuilder(autoGlideMaterial)
                .name(Component.text("Vol automatique", NamedTextColor.AQUA))
                .lore()
                .append(Component.text("Tombez dans le vide pour voler automatiquement", NamedTextColor.GRAY))
                .newline()
                .newline()
                .append(Component.text("Statut: ", NamedTextColor.GRAY))
                .append(Component.text(if (isAutoGlide) "Activé" else "Désactivé", autoGlideColor))
                .buildLore()
                .build()
        ) {
            elytra.toggleAutoGlideEnabled(!isAutoGlide)
            open(viewer)
        }
    }

    private fun placePropulsionButton(elytra: Elytra, viewer: Player) {
        addButton(
            SLOT_PROPULSION, ItemBuilder(Material.FIREWORK_ROCKET)
                .name(Component.text("Propulsion synergique", NamedTextColor.GOLD))
                .lore()
                .append(Component.text("Vous concentrez l'énergie pour vous propulser.", NamedTextColor.BLUE))
                .buildLore()
                .build()
        ) {
            if (!elytra.isInvoked) {
                viewer.sendMessage(
                    Component.text(
                        "Vous devez invoquer vos ailes pour utiliser cette fonction.",
                        NamedTextColor.RED
                    )
                )
                close()
                return@addButton
            }
            elytra.synergicPropulsion(viewer)
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
                player.uniqueId, equipmentService, playerService, playerProfileService, elytraService, this)
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

    @Suppress("MagicNumber")
    private fun placeDecorations() {
        val graySlots = listOf(0, 1, 7, 8, 11, 13, 15, 21, 23, 29, 31, 33, 37, 43)
        val purpleSlots = listOf(2, 10, 18, 28, 38, 6, 16, 26, 34, 42)
        val cyanSlots = listOf(3, 5, 9, 12, 14, 17, 19, 25, 27, 30, 32, 35, 39, 41)

        graySlots.forEach { slot ->
            addButton(slot, ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).build())
        }

        purpleSlots.forEach { slot ->
            addButton(slot, ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE).name(Component.text(" ")).build())
        }

        cyanSlots.forEach { slot ->
            addButton(slot, ItemBuilder(Material.CYAN_STAINED_GLASS_PANE).name(Component.text(" ")).build())
        }
    }

}
