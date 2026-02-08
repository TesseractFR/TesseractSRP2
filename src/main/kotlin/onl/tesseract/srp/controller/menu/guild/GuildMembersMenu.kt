package onl.tesseract.srp.controller.menu.guild

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
import onl.tesseract.srp.domain.territory.guild.GuildMember
import onl.tesseract.srp.domain.territory.guild.enum.GuildRole
import onl.tesseract.srp.domain.territory.guild.enum.GuildRoleChange
import onl.tesseract.srp.service.territory.guild.GuildService
import onl.tesseract.srp.util.PlayerUtils.getPlayerHead
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

private const val PAGE_SIZE = 45
private const val PREV_BUTTON_SLOT = 45
private const val NEXT_BUTTON_SLOT = 53
private val JOINED_DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault())


class GuildMembersMenu(
    private val playerID: UUID,
    private val guildService: GuildService,
    private val menuService: MenuService,
    private val page: Int = 0,
    previous: Menu? = null
) : Menu(MenuSize.Six, "Membres de la guilde".toComponent(), previous) {

    override fun placeButtons(viewer: Player) {
        val guild = guildService.getGuildByMember(viewer.uniqueId) ?: return close()
        val members = sortedMembers(guild)
        val currentPage = currentPage(members.size)
        val viewerRole = guild.getMemberRole(viewer.uniqueId)
        val canManageMembers = viewerRole.canInvite()

        addMembersButtons(viewer, guild, members, currentPage, canManageMembers)
        addPrevPageButton(viewer, currentPage, members.size)
        addNextPageButton(viewer, currentPage, members.size)
        addBackButton()
    }

    private fun sortedMembers(guild: Guild): List<GuildMember> =
        guild.members.sortedWith(
            compareByDescending<GuildMember> { it.playerID == guild.leaderId }
                .thenByDescending { it.role.ordinal }
                .thenBy { it.playerID.toString() }
        )

    private fun currentPage(totalMembers: Int): Int {
        val maxPage = if (totalMembers == 0) 0 else (totalMembers - 1) / PAGE_SIZE
        return page.coerceIn(0, maxPage)
    }

    private fun addMembersButtons(
        viewer: Player,
        guild: Guild,
        members: List<GuildMember>,
        page: Int,
        canManageMembers: Boolean
    ) {
        val start = page * PAGE_SIZE
        val end = (start + PAGE_SIZE).coerceAtMost(members.size)
        members.subList(start, end).forEachIndexed { idx, member ->
            addMemberButton(viewer, guild, idx, member, canManageMembers)
        }
    }

    private fun addMemberButton(
        viewer: Player,
        guild: Guild,
        slot: Int,
        member: GuildMember,
        canManageMembers: Boolean
    ) {
        val joinedDateStr = JOINED_DATE_FORMATTER.format(member.joinedDate)
        val isLeaderViewer = viewer.uniqueId == guild.leaderId
        val canShowActions =
            canManageMembers &&
                    member.playerID != guild.leaderId &&
                    (member.role != GuildRole.Adjoint || isLeaderViewer)
        val lore = ItemLoreBuilder()
            .append("Rôle : ", NamedTextColor.GRAY)
            .append(member.role.displayName, member.role.color)
            .newline()
            .append("A rejoint : ", NamedTextColor.GRAY)
            .append(joinedDateStr, NamedTextColor.WHITE)
            .newline().newline()
        if (canShowActions) {
                lore.append("Clic gauche : ", NamedTextColor.GOLD, TextDecoration.ITALIC)
                .append("Promouvoir", NamedTextColor.GREEN, TextDecoration.ITALIC)
                .newline()
                .append("Clic droit : ", NamedTextColor.GOLD, TextDecoration.ITALIC)
                .append(
                    if (member.role == GuildRole.Citoyen) "Exclure" else "Rétrograder",
                    NamedTextColor.RED, TextDecoration.ITALIC
                )
        }
        addButton(
            slot,
            ItemBuilder(getPlayerHead(member.playerID))
                .name((Bukkit.getOfflinePlayer(member.playerID).name
                    ?: member.playerID.toString()), NamedTextColor.GOLD)
                .lore(lore.get())
                .build()
        ) { click ->
            handleMemberClick(viewer, guild, member, canManageMembers, click.click)
        }
    }

    private fun handleMemberClick(
        viewer: Player,
        guild: Guild,
        member: GuildMember,
        canManageMembers: Boolean,
        clickType: ClickType
    ) {
        if (!canManageMembers) return
        if (member.playerID == guild.leaderId) return

        when (clickType) {
            ClickType.LEFT  -> handleLeftClick(viewer, guild, member)
            ClickType.RIGHT -> handleRightClick(viewer, guild, member)
            else -> { /* No action */ }
        }
    }

    private fun handleLeftClick(viewer: Player, guild: Guild, member: GuildMember) {
        if (member.role == GuildRole.Adjoint) {
            if (viewer.uniqueId != guild.leaderId) return
            val display = Bukkit.getOfflinePlayer(member.playerID).name ?: member.playerID.toString()
            menuService.openConfirmationMenu(
                viewer,
                NamedTextColor.RED + "⚠ Promouvoir $display Chef ?",
                this
            ) {
                if (guildService.changeMemberRole(viewer.uniqueId, member.playerID, GuildRoleChange.PROMOTE)) {
                    open(viewer)
                }
            }
            return
        }
        if (guildService.changeMemberRole(viewer.uniqueId, member.playerID, GuildRoleChange.PROMOTE)) {
            open(viewer)
        }
    }

    private fun handleRightClick(viewer: Player, guild: Guild, member: GuildMember) {
        val isLeaderViewer = viewer.uniqueId == guild.leaderId
        if (member.role == GuildRole.Adjoint && !isLeaderViewer) return
        if (member.role == GuildRole.Citoyen) {
            val target = Bukkit.getOfflinePlayer(member.playerID)
            val arg = target.name ?: target.uniqueId.toString()
            viewer.performCommand("guild kick $arg")
            return
        }
        if (guildService.changeMemberRole(viewer.uniqueId, member.playerID, GuildRoleChange.DEMOTE)) {
            open(viewer)
        }
    }

    private fun addPrevPageButton(viewer: Player, currentPage: Int, totalMembers: Int) {
        if (currentPage <= 0) return
        val maxPage = if (totalMembers == 0) 0 else (totalMembers - 1) / PAGE_SIZE
        addButton(
            PREV_BUTTON_SLOT,
            ItemBuilder(Material.ARROW)
                .name("Page précédente", NamedTextColor.YELLOW)
                .lore("Page ${currentPage + 1} / ${maxPage + 1}")
                .build()
        ) {
            GuildMembersMenu(playerID, guildService, menuService, currentPage - 1, this).open(viewer)
        }
    }

    private fun addNextPageButton(viewer: Player, currentPage: Int, totalMembers: Int) {
        val maxPage = if (totalMembers == 0) 0 else (totalMembers - 1) / PAGE_SIZE
        if (currentPage >= maxPage) return
        addButton(
            NEXT_BUTTON_SLOT,
            ItemBuilder(Material.ARROW)
                .name("Page suivante", NamedTextColor.YELLOW)
                .lore("Page ${currentPage + 1} / ${maxPage + 1}")
                .build()
        ) {
            GuildMembersMenu(playerID, guildService, menuService,currentPage + 1, this).open(viewer)
        }
    }
}
