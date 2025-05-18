package onl.tesseract.srp.controller.menu.elytra

import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.menu.Button
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.profile.PlayerProfileService
import onl.tesseract.lib.util.ItemLoreBuilder
import onl.tesseract.lib.util.menu.InventoryHeadIcons
import onl.tesseract.lib.util.plus
import onl.tesseract.lib.util.toComponent
import onl.tesseract.srp.domain.elytra.EnumElytraUpgrade
import onl.tesseract.srp.service.player.SrpPlayerService
import onl.tesseract.srp.util.menu.BiMenu
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*

class ElytraUpgradeMenu(
    val playerID: UUID,
    private val playerService: SrpPlayerService,
    private val profileService: PlayerProfileService,
    upgrade: EnumElytraUpgrade,
    previous: Menu?
) : BiMenu(MenuSize.One, upgrade.displayName.toComponent(), previous) {

    private var scroll = 0

    override fun placeButtons(viewer: Player) {
        draw(scroll, viewer)
    }

    private fun draw(scroll: Int, viewer: Player) {
        clear()

        val player = playerService.getPlayer(playerID)
        val buttons = buildFakeLine()
        val safeScroll = scroll.coerceIn(0, (buttons.size - 9).coerceAtLeast(0))

        for (slot in 0 until 9) {
            val index = safeScroll + slot
            if (index in buttons.indices) {
                addButton(slot, buttons[index])
            }
        }

        addBottomButton(21, ItemBuilder(Material.PLAYER_HEAD)
            .customHead(InventoryHeadIcons.LEFT_ARROW.data, InventoryHeadIcons.LEFT_ARROW.signature)
            .name("Gauche")
            .build()
        ) {
            draw(safeScroll - 1, viewer)
        }

        addBottomButton(23, ItemBuilder(Material.PLAYER_HEAD)
            .customHead(InventoryHeadIcons.RIGHT_ARROW.data, InventoryHeadIcons.RIGHT_ARROW.signature)
            .name("Droite")
            .build()
        ) {
            draw(safeScroll + 1, viewer)
        }

        addBottomButton(4, ItemBuilder(Material.PISTON)
            .name("Affichage compacte")
            .lore()
            .newline()
            .append("Clique pour voir tous les niveaux", NamedTextColor.GRAY)
            .buildLore()
            .build()
        ) {}

        addBottomButton(9, {
            ItemBuilder(profileService.getPlayerHead(playerID))
                .name("Mes informations", NamedTextColor.GREEN)
                .lore()
                .newline()
                .addField("Argent", NamedTextColor.GOLD + "${player.money} Lys")
                .addField("Grade", NamedTextColor.GOLD + "${player.rank}")
                .buildLore()
                .build()
        })

        addBottomButton(17, ItemBuilder(Material.OAK_SIGN)
            .name("Comment utiliser ce menu ?")
            .lore()
            .newline()
            .append(
                NamedTextColor.GRAY + "La vue du haut montre les niveaux. Utilise les flèches pour te déplacer de gauche à droite."
            )
            .buildLore()
            .build()
        )

        addBottomBackButton()
        addBottomCloseButton()
    }

    private fun buildFakeLine(): List<Button> {
        val buttons = mutableListOf<Button>()
        for (i in 0 until 10) {
            buttons.add(Button(
                ItemBuilder(Material.BEACON)
                    .name("Niveau ${i + 1}")
                    .lore(ItemLoreBuilder().newline().append("Niveau visuel uniquement", NamedTextColor.GRAY).get())
                    .build()
            ))

            if (i < 9) {
                buttons.addAll(listOf(
                    Button(ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("").build()),
                    Button(ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("").build()),
                    Button(ItemBuilder(Material.RED_STAINED_GLASS_PANE).name("").build()),
                ))
            }
        }
        return buttons
    }
}
