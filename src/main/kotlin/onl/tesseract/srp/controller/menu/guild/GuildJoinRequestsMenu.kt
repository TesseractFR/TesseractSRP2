package onl.tesseract.srp.controller.menu.guild

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import onl.tesseract.lib.menu.ItemBuilder
import onl.tesseract.lib.menu.Menu
import onl.tesseract.lib.menu.MenuService
import onl.tesseract.lib.menu.MenuSize
import onl.tesseract.lib.util.ItemLoreBuilder
import onl.tesseract.lib.util.plus
import onl.tesseract.lib.util.toComponent
import onl.tesseract.srp.domain.territory.guild.Guild
import onl.tesseract.srp.domain.territory.guild.GuildJoinRequest
import onl.tesseract.srp.service.territory.guild.GuildService
import onl.tesseract.srp.util.GuildChatSuccess
import onl.tesseract.srp.util.PlayerUtils.getPlayerHead
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import java.util.UUID

private const val PAGE_SIZE = 27
private const val PREV_BUTTON_SLOT = 18
private const val NEXT_BUTTON_SLOT = 26
private const val MAX_JOIN_MESSAGE_LENGTH = 120

class GuildJoinRequestsMenu(
    private val playerID: UUID,
    private val guildService: GuildService,
    private val menuService: MenuService,
    private val page: Int = 0,
    previous: Menu? = null
) : Menu(MenuSize.Three, "Demandes en attente".toComponent(), previous) {

    override fun placeButtons(viewer: Player) {
        val guild = guildService.getGuildByMember(viewer.uniqueId) ?: return close()
        val viewerRole = guild.getMemberRole(viewer.uniqueId)
        val canManageRequests = viewerRole.canInvite()
        val requests = sortRequests(guild)
        val currentPage = currentPage(requests.size)
        addRequestButtons(viewer, guild, requests, currentPage, canManageRequests)
        addPrevPageButton(viewer, currentPage, requests.size)
        addNextPageButton(viewer, currentPage, requests.size)
        addBackButton()
    }

    private fun sortRequests(guild: Guild): List<GuildJoinRequest> =
        guild.joinRequests.sortedWith(
            compareByDescending<GuildJoinRequest> { it.requestedDate }
                .thenBy { it.playerID.toString() })

    private fun currentPage(total: Int): Int {
        val maxPage = if (total == 0) 0 else (total - 1) / PAGE_SIZE
        return page.coerceIn(0, maxPage)
    }

    private fun addRequestButtons(
        viewer: Player,
        guild: Guild,
        requests: List<GuildJoinRequest>,
        page: Int,
        canManageRequests: Boolean
    ) {
        val start = page * PAGE_SIZE
        val end = (start + PAGE_SIZE).coerceAtMost(requests.size)
        requests.subList(start, end).forEachIndexed { index, req ->
            addRequestButton(viewer, guild, index, req, canManageRequests)
        }
    }

    private fun addRequestButton(
        viewer: Player,
        guild: Guild,
        slot: Int,
        request: GuildJoinRequest,
        canManageRequests: Boolean
    ) {
        val playerId = request.playerID
        val name = Bukkit.getOfflinePlayer(playerId).name ?: playerId.toString()
        val displayMsg = request.message
            .trim()
            .ifBlank { "—" }
            .let { if (it.length > MAX_JOIN_MESSAGE_LENGTH) it.take(MAX_JOIN_MESSAGE_LENGTH - 1) + "…" else it }
        val lore = ItemLoreBuilder()
            .append(displayMsg, NamedTextColor.WHITE, TextDecoration.ITALIC)
            .newline()
        if (canManageRequests) {
            lore.newline()
                .append("Clic gauche : ", NamedTextColor.GOLD)
                .append("Accepter", NamedTextColor.GREEN)
                .newline()
                .append("Clic droit : ", NamedTextColor.GOLD)
                .append("Refuser", NamedTextColor.RED)
        }
        addButton(
            slot,
            ItemBuilder(getPlayerHead(playerId))
                .name(name, NamedTextColor.GOLD)
                .lore(lore.get())
                .build()
        ) { click ->
            if (!canManageRequests) return@addButton
            when (click.click) {
                ClickType.LEFT -> {
                    menuService.openConfirmationMenu(
                        viewer,
                        Component.text("Accepter la demande de $name ?"),
                        this
                    ) {
                        guildService.acceptJoinRequest(viewer.uniqueId, playerId)
                        GuildJoinRequestsMenu(playerID, guildService, menuService, page, previous).open(viewer)
                        Bukkit.getOfflinePlayer(playerId).player?.sendMessage(
                            GuildChatSuccess + "Votre demande pour rejoindre la guilde ${guild.name} a été acceptée !")
                    }
                }
                ClickType.RIGHT -> {
                    menuService.openConfirmationMenu(
                        viewer,
                        Component.text("Refuser la demande de $name ?"),
                        this
                    ) {
                        guildService.declineJoinRequest(guild.name, playerId)
                        GuildJoinRequestsMenu(playerID, guildService, menuService, page, previous).open(viewer)
                    }
                }
                else -> {/* No action */ }
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
            GuildJoinRequestsMenu(playerID, guildService, menuService, currentPage - 1, this).open(viewer)
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
            GuildJoinRequestsMenu(playerID, guildService, menuService, currentPage + 1, this).open(viewer)
        }
    }

}
