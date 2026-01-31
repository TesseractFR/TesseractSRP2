package onl.tesseract.srp.controller.menu.elytra

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.core.cosmetics.menu.ElytraTrailSelectionMenu
import onl.tesseract.lib.equipment.EquipmentService
import onl.tesseract.lib.event.equipment.invocable.Elytra
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.profile.PlayerProfileService
import onl.tesseract.lib.util.ChatFormats.ELYTRA_ERROR
import onl.tesseract.lib.util.ChatFormats.ELYTRA_SUCCESS
import onl.tesseract.lib.util.plus
import onl.tesseract.srp.domain.equipment.elytra.ElytraInvocationResult
import onl.tesseract.srp.service.equipment.elytra.ElytraService
import onl.tesseract.srp.service.player.SrpPlayerService
import onl.tesseract.srp.util.PlayerUtils.tryFreeChestplateSlot
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
    private val equipmentService: EquipmentService,
    private val srpPlayerService: SrpPlayerService
) : ElytraBaseMenu(MenuSize.Five, Component.text("Ailes Célestes", NamedTextColor.DARK_PURPLE)) {

    override fun placeButtons(viewer: Player) {
        val elytra = getElytra(player)
        val autoGlide = elytra?.autoGlide ?: false
        placeDecorations()
        placeElytraInvokeButton(viewer)
        placeAutoGlideButton(autoGlide, viewer)
        placePropulsionButton(viewer)
        placeUpgradeButton(viewer)
        placeSillageButton(viewer)

        addBackButton()
        addCloseButton()
    }

    private fun placeElytraInvokeButton(viewer: Player) {
        addButton(
            SLOT_FLANC_ETHERE, ItemBuilder(Material.ELYTRA)
                .name("Flanc éthéré", NamedTextColor.LIGHT_PURPLE)
                .lore()
                .append("Invoque ou désinvoque vos ailes divines.", NamedTextColor.GRAY)
                .buildLore()
                .build()
        ) {
            val result = elytraService.getInvocationResult(viewer.uniqueId)
            when (result) {
                ElytraInvocationResult.NO_ELYTRA -> {
                    if (!canEquipElytra(viewer)) return@addButton
                    elytraService.createElytra(viewer.uniqueId)
                    invokeElytra(viewer)
                }
                ElytraInvocationResult.ALREADY_INVOKED -> {
                    val elytra = getElytra(viewer, true) ?: return@addButton
                    equipmentService.uninvoke(viewer, elytra)
                }
                ElytraInvocationResult.READY_TO_INVOKE -> {
                    if (!canEquipElytra(viewer)) return@addButton
                    invokeElytra(viewer)
                }
            }
            close()
        }
    }

    private fun canEquipElytra(viewer: Player): Boolean {
        if (!tryFreeChestplateSlot(viewer)) {
            viewer.closeInventory()
            viewer.sendMessage(
                ELYTRA_ERROR + "Ton inventaire est plein, impossible d'invoquer tes Ailes Célestes."
            )
            return false
        }
        return true
    }

    private fun invokeElytra(viewer: Player) {
        equipmentService.invoke(viewer, Elytra::class.java, null, true)
        viewer.sendMessage(ELYTRA_SUCCESS + "Tu as invoqué tes Ailes Célestes !")
    }


    private fun placeAutoGlideButton(autoGlide: Boolean, viewer: Player) {
        val autoGlideColor = if (autoGlide) NamedTextColor.GREEN else NamedTextColor.RED
        val autoGlideMaterial = if (autoGlide) Material.LIME_DYE else Material.GRAY_DYE
        addButton(
            SLOT_AUTO_GLIDE, ItemBuilder(autoGlideMaterial)
                .name("Vol automatique", NamedTextColor.AQUA)
                .lore()
                .append("Tombez dans le vide pour voler automatiquement", NamedTextColor.GRAY)
                .newline()
                .newline()
                .append("Statut: ", NamedTextColor.GRAY)
                .append(if (autoGlide) "Activé" else "Désactivé", autoGlideColor)
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
                .name("Propulsion synergique", NamedTextColor.GOLD)
                .lore()
                .append("Vous concentrez l'énergie pour vous propulser.", NamedTextColor.BLUE)
                .buildLore()
                .build()
        ) {
            val canPropulse = elytraService.requestPropulsion(viewer.uniqueId)
            if (!canPropulse) {
                viewer.sendMessage(ELYTRA_ERROR + "Vous devez invoquer vos ailes pour utiliser cette fonction.")
            } else {
                val elytra = getElytra(viewer, true) ?: return@addButton
                elytra.synergicPropulsion(viewer)
            }
            close()
        }
    }

    private fun placeUpgradeButton(viewer: Player) {
        addButton(
            SLOT_AMELIORATIONS, ItemBuilder(Material.PRISMARINE_CRYSTALS)
                .name("Améliorations", NamedTextColor.GOLD)
                .lore()
                .append("Affiche les améliorations disponibles.", NamedTextColor.GRAY)
                .buildLore()
                .build()
        ) {
            ElytraUpgradeMenu(
                player.uniqueId, playerProfileService, elytraService, srpPlayerService, this)
                .open(viewer)
        }
    }

    private fun placeSillageButton(viewer: Player) {
        addButton(
            SLOT_SILLAGE, ItemBuilder(Material.NETHER_STAR)
                .name("Sillage des ailes", NamedTextColor.DARK_AQUA)
                .lore()
                .append("Affiche des particules pendant le vol.", NamedTextColor.GRAY)
                .buildLore()
                .build()
        ) {
            ElytraTrailSelectionMenu(viewer.uniqueId, this).open(viewer)
        }
    }

    private fun getElytra(viewer: Player, invokedOnly: Boolean = false): Elytra? {
        val equipment = equipmentService.getEquipment(viewer.uniqueId)
        val elytra = equipment.get(Elytra::class.java) ?: return null
        return if (!invokedOnly || elytra.isInvoked) elytra else null
    }

}
