package onl.tesseract.srp.controller.menu.campement

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuService
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.util.ItemLoreBuilder
import onl.tesseract.lib.util.plus
import onl.tesseract.lib.util.toComponent
import onl.tesseract.srp.service.territory.campement.CampementService
import onl.tesseract.srp.util.PlayerUtils.getPlayerHead
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import java.util.*

private const val PAGE_SIZE = 27
private const val PREV_BUTTON_SLOT = 27
private const val NEXT_BUTTON_SLOT = 35

class CampementTrustedPlayersMenu(
    private val playerID: UUID,
    private val campementService: CampementService,
    private val menuService: MenuService,
    private val page: Int = 0,
    previous: Menu? = null
) : Menu(MenuSize.Three, "Joueurs de confiance".toComponent(), previous) {

    override fun placeButtons(viewer: Player) {
        val campement = campementService.getCampementByOwner(playerID) ?: return close()
        val trusted = campement.getTrusted().sortedBy {
            Bukkit.getOfflinePlayer(it).name ?: it.toString()
        }
        val currentPage = currentPage(trusted.size)
        val isOwner = viewer.uniqueId == playerID

        addTrustedButtons(viewer, trusted, currentPage, isOwner)
        addPrevPageButton(viewer, currentPage, trusted.size)
        addNextPageButton(viewer, currentPage, trusted.size)
        addBackButton()
    }

    private fun currentPage(total: Int): Int {
        val maxPage = if (total == 0) 0 else (total - 1) / PAGE_SIZE
        return page.coerceIn(0, maxPage)
    }

    private fun addTrustedButtons(viewer: Player, trusted: List<UUID>, page: Int, isOwner: Boolean) {
        val start = page * PAGE_SIZE
        val end = (start + PAGE_SIZE).coerceAtMost(trusted.size)
        trusted.subList(start, end).forEachIndexed { idx, uuid ->
            addTrustedButton(viewer, idx, uuid, isOwner)
        }
    }

    private fun addTrustedButton(viewer: Player, slot: Int, uuid: UUID, isOwner: Boolean) {
        val name = Bukkit.getOfflinePlayer(uuid).name ?: uuid.toString()
        val lore = ItemLoreBuilder()
            .append("Joueur de confiance", NamedTextColor.GRAY)
        if (isOwner) {
            lore.newline()
                .append("Clic droit : ", NamedTextColor.GOLD, TextDecoration.ITALIC)
                .append("Retirer", NamedTextColor.RED, TextDecoration.ITALIC)
        }
        addButton(
            slot,
            ItemBuilder(getPlayerHead(uuid))
                .name(name, NamedTextColor.GOLD)
                .lore(lore.get())
                .build()
        ) { click ->
            if (isOwner && click.click == ClickType.RIGHT) {
                menuService.openConfirmationMenu(
                    viewer,
                    NamedTextColor.RED + "⚠ Retirer $name de la liste de confiance ?",
                    this
                ) {
                    campementService.untrust(playerID, uuid)
                    CampementTrustedPlayersMenu(playerID, campementService, menuService, page, this.previous).open(viewer)
                }
            }
        }
    }

    private fun addPrevPageButton(viewer: Player, currentPage: Int, total: Int) {
        if (currentPage <= 0) return
        val maxPage = if (total == 0) 0 else (total - 1) / PAGE_SIZE
        addButton(
            PREV_BUTTON_SLOT,
            ItemBuilder(Material.ARROW)
                .name("Page précédente", NamedTextColor.YELLOW)
                .lore("Page ${currentPage + 1} / ${maxPage + 1}")
                .build()
        ) {
            CampementTrustedPlayersMenu(playerID, campementService, menuService, currentPage - 1, this).open(viewer)
        }
    }

    private fun addNextPageButton(viewer: Player, currentPage: Int, total: Int) {
        val maxPage = if (total == 0) 0 else (total - 1) / PAGE_SIZE
        if (currentPage >= maxPage) return
        addButton(
            NEXT_BUTTON_SLOT,
            ItemBuilder(Material.ARROW)
                .name("Page suivante", NamedTextColor.YELLOW)
                .lore("Page ${currentPage + 1} / ${maxPage + 1}")
                .build()
        ) {
            CampementTrustedPlayersMenu(playerID, campementService, menuService, currentPage + 1, this).open(viewer)
        }
    }
}