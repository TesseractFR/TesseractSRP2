package onl.tesseract.srp.controller.menu.elytra

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.util.toComponent
import onl.tesseract.srp.domain.elytra.EnumElytraUpgrade
import org.bukkit.entity.Player

class ElytraUpgradeSelectionMenu(
    title: String,
    previous: Menu? = null,
    private val onUpgradeClick: (viewer: Player, upgrade: EnumElytraUpgrade) -> Unit
) : Menu(MenuSize.One, title.toComponent(), previous) {

    override fun placeButtons(viewer: Player) {
        addBackButton()
        addCloseButton()

        EnumElytraUpgrade.entries.forEachIndexed { index, upgrade ->
            addButton(index, ItemBuilder(upgrade.material)
                .name(Component.text(upgrade.displayName, NamedTextColor.AQUA))
                .build()
            ) {
                onUpgradeClick(viewer, upgrade)
            }
        }

        super.placeButtons(viewer)
    }
}
